# Plano de implementação — AutoCare local + anúncios + Play Store

Objetivo: publicar o AutoCare na Google Play com **dados 100% locais** (sem Firebase,
sem Cloudinary, sem login), **múltiplos perfis no aparelho**, **Auto Backup +
exportar/importar** e **monetização por anúncios (AdMob)**.

## Decisões já tomadas

| Tema | Decisão |
|---|---|
| Conta | Não existe conta remota. O app abre direto, sem login. |
| Perfis | Vários perfis locais no mesmo aparelho (ex.: eu, filho, esposa). Cada veículo pertence a um perfil. |
| Persistência | Room (veículos, manutenções, perfis) + DataStore (perfil ativo, preferências). |
| Fotos | Arquivos em `filesDir/images/`, **sempre JPEG com qualidade 75 (faixa 70–80) e redimensionados**, nunca o arquivo original ou bruto. O banco guarda o caminho **relativo** (`images/<uuid>.jpg`). Detalhes na etapa 3. |
| Firebase | Removido por completo: Auth, Realtime Database, Analytics, Messaging, plugin google-services. Sem Crashlytics. |
| Backup | Auto Backup do Android ligado (banco + fotos) **e** exportar/importar em `.zip`. |
| Receita | AdMob (banner + intersticial com limite de frequência) com consentimento via UMP. |
| Início | Mostra as pendências de **todos os perfis**, com o nome do dono no card e um filtro por perfil. A aba Veículos fica restrita ao perfil ativo. |
| Importação | **Mescla por id**, não destrutiva, sempre com prévia antes de aplicar. |
| Conta Play | **Organização**: não exige o teste fechado com 12 testadores, mas exige D-U-N-S e verificação da organização. |

## Regras gerais de cada etapa

- **Uma branch e um PR por etapa** (`feat/etapa-N-<assunto>`), com merge por squash, como já acontece no histórico (`(#4)`, `(#5)`…).
- Mensagens de commit em português, no imperativo, no padrão do repositório (`Adiciona…`, `Remove…`, `Sobe…`).
- **Portão mínimo antes de qualquer commit** (o mesmo do CI):
  ```sh
  ./gradlew testDebugUnitTest lintDebug assembleDebug
  ```
- **Portão de release** nas etapas que mexem em dependências, reflexão ou R8:
  ```sh
  ./gradlew lintRelease assembleRelease   # sem keystore, gera o APK não assinado
  ```
  Instale o APK de release num emulador e abra as telas afetadas. Um problema de R8 só aparece aqui.
- **Testes instrumentados** (emulador) nas etapas marcadas com 📱:
  ```sh
  ./gradlew connectedDebugAndroidTest
  ```
- O app ainda **não foi publicado**. Não há usuário nem dado para migrar, então o banco começa na versão 1 e o código remoto pode ser apagado sem camada de compatibilidade.

---

## Etapa 1 — Toolchain atualizado (sem mudar o comportamento)

**Por quê:** o Room/KSP, o SDK de anúncios e o `targetSdk` exigido pela Play pedem um AGP e um Kotlin mais novos. Fazer isso antes, isolado, evita misturar erro de build com erro de funcionalidade.

**Mudanças**
- [ ] Gradle wrapper `8.4` → versão exigida pelo AGP escolhido (`gradle/wrapper/gradle-wrapper.properties`).
- [ ] AGP `8.3.2` → 8.9 ou mais recente (suporta `compileSdk 36`).
- [ ] Kotlin `1.9.24` → 2.x com o plugin `org.jetbrains.kotlin.plugin.compose`, que substitui `composeOptions.kotlinCompilerExtensionVersion` e `compose_compiler_version`.
- [ ] `compileSdk 34` → 36. **O `targetSdk` fica em 34 por enquanto** (ele sobe na etapa 9).
- [ ] Java/JVM target `1.8` → 17.
- [ ] Compose BOM, `core-ktx`, `activity-compose`, `lifecycle`, `navigation` e `work-runtime` nas versões estáveis compatíveis.
- [ ] Confirmar que `.github/actions/setup-android` continua com o JDK 17 (já está).

**Testes**
- Suíte existente, sem alteração. Ela é a rede de segurança desta etapa.

**Validação**
- [ ] Portão mínimo e portão de release verdes.
- [ ] 📱 `connectedDebugAndroidTest` verde.
- [ ] App rodando no emulador: login, lista, detalhe, formulário e notificação continuam funcionando.

**Commit:** `Sobe AGP, Kotlin 2 e compileSdk 36`

---

## Etapa 2 — Camada de dados local (Room + catálogo de serviços)

**Por quê:** criar a nova fonte de dados **ao lado** da atual, testada e ainda sem ligação com a UI. A troca acontece na etapa 4.

**Modelo**

```
profiles     (id TEXT PK uuid, name TEXT, photo_path TEXT?, created_at INTEGER)
vehicles     (id TEXT PK uuid, profile_id TEXT FK→profiles ON DELETE CASCADE [index],
              nick_name, brand, model, plate, photo_path, average_distance_per_month)
maintenances (id INTEGER PK — mantém o Int aleatório atual, que também é requestCode
              do PendingIntent e id da notificação,
              vehicle_id TEXT FK→vehicles ON DELETE CASCADE [index],
              description, date, current_mileage, forecast_next_exchange_mileage,
              forecast_next_exchange_date, comments, completed)
```

- Os IDs de perfil e de veículo são UUID para que o **importar com mescla** (etapa 8) não colida com dados de outro aparelho.
- As datas continuam em *epoch day*, como hoje, para não mexer em `MaintenanceProgress`.

**Mudanças**
- [ ] Dependências: Room (`runtime`, `ktx`, `compiler` via KSP) e o plugin KSP.
- [ ] `data/local/`: `AutoCareDatabase` (`exportSchema = true`, com `room.schemaLocation` em `app/schemas/`, **versionado no git**), entidades, `ProfileDao`, `VehicleDao` e `MaintenanceDao`, com `Flow` para observação e `suspend` para escrita.
- [ ] Mapeadores entidade ↔ modelo de domínio (`Vehicle`, `Maintenance`). `User` vira `Profile`.
- [ ] `assets/services.json` com o conteúdo de `lista-servicos` e um `LocalServicesRepository` que lê esse arquivo.
- [ ] Novas interfaces de repositório **sem `userId` e sem callbacks** (`suspend`/`Flow`): `ProfileRepository`, `VehicleRepository`, `MaintenanceRepository`, `ServicesRepository`. Implementações `Room*`.
- [ ] Operações compostas em `@Transaction`, por exemplo "transferir veículo para outro perfil" e "restaurar veículo com manutenções" (o desfazer do snackbar).

**Testes**
- [ ] Unitários: mapeadores (ida e volta, campos nulos) e parse de `services.json` (já há `org.json` real no classpath de teste).
- [ ] 📱 Instrumentados com o Room `inMemoryDatabaseBuilder`:
  - excluir um perfil apaga os veículos e as manutenções dele (cascata);
  - `observeVehicles(profileId)` emite só os veículos do perfil e reemite após uma inserção;
  - transferir um veículo muda o dono e mantém as manutenções;
  - restaurar um veículo excluído com o mesmo id devolve as manutenções;
  - consulta de manutenções pendentes com data prevista, de **todos** os perfis (usada pelo worker).

**Validação**
- [ ] Portão mínimo verde e `app/schemas/.../1.json` gerado e commitado.

**Commit:** `Adiciona banco Room com perfis, veiculos e manutencoes`

---

## Etapa 3 — Armazenamento local de fotos

**Política de imagem** (vale para toda foto salva pelo app: perfil e veículo)

| Parâmetro | Foto de veículo | Foto de perfil | Por quê |
|---|---|---|---|
| Formato | JPEG | JPEG | As fotos são opacas; PNG ou o bitmap bruto ocupariam de 5 a 20 vezes mais. |
| Qualidade | **75** | **75** | Na faixa de 70–80, a perda não é visível em tela de celular. Hoje está em 85, quase o dobro de bytes para ganho imperceptível. |
| Maior lado | **1280 px** | **512 px** | O card e o detalhe nunca exibem acima disso. O avatar aparece com no máximo ~120 dp. |
| Alvo de tamanho | ~150–300 KB | ~30–60 KB | Cerca de 100 fotos de veículo cabem nos 25 MB do Auto Backup (etapa 7). |
| Metadados EXIF | removidos | removidos | A recodificação descarta o EXIF, incluindo a **localização GPS** da foto (privacidade). A orientação é aplicada no bitmap antes. |

- A qualidade e o lado máximo ficam como constantes nomeadas em `ImageCompressor` (`VEHICLE_PHOTO`, `PROFILE_PHOTO`), e não como números soltos nas telas.
- **Nenhum caminho grava a imagem original.** O `LocalImageStore.save` só aceita a saída do `ImageCompressor`, e não existe API para copiar a `Uri` crua.
- O cropper (`CropImageOptions`, em `VehicleDetailsScreen.kt` e `AccountScreen.kt`) passa a usar `outputCompressFormat = JPEG` e `outputCompressQuality = 90`. Essa é só a imagem intermediária, no cache, e é recomprimida em seguida. Ela é **apagada logo depois** de salva no `filesDir`, porque hoje esses arquivos se acumulam no cache.

**Mudanças**
- [ ] `ImageCompressor`: qualidade padrão 85 → **75**, lado máximo parametrizado por tipo (1280 / 512) e correção de orientação pelo EXIF antes de recodificar.
- [ ] `LocalImageStore`:
  - `save(uri, kind): String` comprime com o `ImageCompressor` conforme o tipo, grava em `images/<uuid>.jpg` e devolve o caminho relativo;
  - `resolve(path): File`;
  - `delete(path)`;
  - `sweepOrphans(referenced: Set<String>)` apaga arquivos que nenhum registro referencia.
- [ ] A **exclusão física é feita pela varredura de órfãos** (na abertura do app), e não no momento da exclusão. Assim o "desfazer" do snackbar continua funcionando, porque a foto ainda existe enquanto ele está na tela.
- [ ] A foto substituída também vira órfã e é recolhida pela varredura.
- [ ] O Coil passa a carregar um `File` em vez de uma URL. A interface `ImageUploader` é substituída.

**Testes**
- [ ] 📱 Instrumentados: salvar gera um arquivo dentro de `filesDir/images`; `resolve` devolve o mesmo arquivo; `sweepOrphans` apaga só os não referenciados.
- [ ] 📱 Instrumentados de compressão (estender `ImageCompressorTest`), com uma foto de teste grande (ex.: 4000×3000):
  - o arquivo salvo começa com o marcador JPEG `FF D8 FF`, ou seja, nunca é PNG nem bruto;
  - o maior lado é ≤ 1280 (veículo) e ≤ 512 (perfil), e a proporção é preservada;
  - o arquivo salvo tem **menos de 400 KB** (veículo) e **menos de 100 KB** (perfil), um teto folgado para pegar regressão;
  - o arquivo salvo é menor que o original;
  - uma imagem com EXIF de rotação 90° sai na orientação correta e sem o EXIF de GPS;
  - o arquivo intermediário do cropper não existe mais depois do `save`.

**Validação (manual)**
- [ ] Tirar 3 fotos reais pela câmera e 3 da galeria, abrir o card e o detalhe e conferir a nitidez (sem blocos ou serrilhado visíveis).
- [ ] `adb shell run-as br.com.gabrielmorais.autocare ls -l files/images` e registrar no PR o tamanho médio por foto.
- [ ] `adb shell run-as br.com.gabrielmorais.autocare ls cache` sem sobras do cropper.
- [ ] Unitário: `sweepOrphans` nunca apaga fora de `images/`, e caminhos com `..` são rejeitados.

**Validação**
- [ ] Portão mínimo verde.

**Commit:** `Adiciona armazenamento local de fotos`

---

## Etapa 4 — App passa a usar os dados locais (sem login)

**Por quê:** é a virada. A partir daqui o app funciona offline, com um perfil.

**Mudanças**
- [ ] `ActiveProfileStore` (DataStore Preferences) guarda o `activeProfileId`.
- [ ] **Primeiro acesso:** uma tela "Como você se chama?" (nome + foto opcional) cria o primeiro perfil e o marca como ativo.
- [ ] `MainActivity` vira a launcher, com o tema `Theme.AutoCare.Splash`. A splash fica na tela até descobrir se existe perfil, e depois mostra o onboarding ou o início.
- [ ] `MainViewModel`, `VehicleDetailsViewModel` e `AddMaintenanceViewModel` passam a usar os novos repositórios. Saem `authRepository`, `currentUserId` e as mensagens de "Sessão expirada".
- [ ] A tela de conta vira **Perfil**: nome e foto editáveis. Saem logout e troca de senha.
- [ ] Notificações:
  - o `RescheduleNotificationsWorker` lê do Room as manutenções pendentes de **todos** os perfis;
  - o `BootReceiver` perde a restrição `NetworkType.CONNECTED`, que existia por causa do Firebase;
  - a varredura de fotos órfãs roda no mesmo worker, ao abrir o app.

**Testes**
- [ ] Unitários de ViewModel com fakes dos novos repositórios. Reescrever `MainViewModelTest` e `MainViewModelVehiclesTest` e criar fakes em `test/fakes/`.
  - sem perfil → estado de onboarding; com perfil → estado de início;
  - salvar, excluir e desfazer exclusão de veículo;
  - editar o perfil atualiza nome e foto.
- [ ] 📱 UI: o onboarding cria o perfil e leva ao início. Os testes de formulário e de card continuam passando.

**Validação**
- [ ] Portão mínimo e portão de release verdes.
- [ ] Manual, em modo avião: instalar do zero → onboarding → cadastrar veículo com foto → cadastrar manutenção → reiniciar o emulador → a notificação continua agendada (`adb shell dumpsys alarm | grep autocare`).

**Commit:** `Troca Firebase por dados locais e remove o login`

---

## Etapa 5 — Remoção do Firebase, Cloudinary e código morto

**Mudanças**
- [ ] Apagar `LoginActivity`, `RegisterActivity`, `LoginViewModel`, `RegisterViewModel`, `LoginViewModelFactory`, `LoginState`, `LoginUiState`, `CredentialValidator`, `PasswordTextField`, `AuthRepository*`, `UserRepository*` e as implementações Firebase dos demais repositórios, `FirebaseModule`, `data/images/Cloudinary*` e `OkHttpClient`.
- [ ] Testes órfãos: `CloudinaryUploadApiTest`, `CloudinaryUrlTest`, `CredentialValidatorTest`, `FakeAuthRepository` e `FakeUserRepository` (antigo).
- [ ] Gradle: tirar o plugin e o classpath `google-services`, o BoM do Firebase e todas as dependências `firebase-*`, `okhttp`, `mockwebserver`, e os `buildConfigField` do Cloudinary.
- [ ] `proguard-rules.pro`: apagar as regras do Firebase e de `data.models.**`. O Room gera código e não usa reflexão.
- [ ] Manifesto: remover a `RegisterActivity` e qualquer resquício de Firebase.
- [ ] Repositório: apagar `firebase.json`, `.firebaserc`, `database.rules.json` e `.github/workflows/deploy-database-rules.yml`.
- [ ] CI (`setup-android`, `ci.yml`, `instrumented-tests.yml`, `release.yml`): tirar os inputs e secrets `GOOGLE_SERVICES_JSON_BASE64` e `CLOUDINARY_*` e a geração do placeholder do `google-services.json`.
- [ ] `strings.xml`: remover as strings de login, cadastro e senha. O lint `UnusedResources` ajuda a achar.
- [ ] `README.md`: reescrever a seção de configuração. Não é mais preciso configurar nada para compilar.

**Testes**
- Nenhum teste novo. A suíte inteira deve continuar verde depois da remoção.

**Validação**
- [ ] `grep -ri -E "firebase|cloudinary" app/ .github/` sem resultados (fora do histórico).
- [ ] Portão mínimo e portão de release verdes. Comparar o tamanho do APK de release antes e depois (deve cair bastante).
- [ ] O CI passa **sem nenhum secret** configurado (exceto no release).

**Commit:** `Remove Firebase, Cloudinary e o fluxo de login`

---

## Etapa 6 — Múltiplos perfis

**Comportamento**
- O perfil ativo aparece no topo da tela de Perfil e num seletor (avatar) na barra superior do Início e de Veículos.
- **Veículos:** lista só os veículos do perfil ativo.
- **Início (pendências):** mostra as pendências de **todos os perfis**, ordenadas por urgência. Quando há mais de um perfil, cada card leva o nome ou avatar do dono. Um filtro em chips ("Todos", "Ana", "Pedro"…) começa em "Todos". Tocar no card abre o veículo e torna o perfil dele o ativo.
- **Gerenciar perfis:** criar, editar nome e foto e excluir.
  - A exclusão pede confirmação e informa quantos veículos e manutenções serão apagados.
  - O último perfil não pode ser excluído.
  - Excluir o perfil ativo troca para outro perfil.
- **Transferir veículo** (no detalhe do veículo): mover para outro perfil, mantendo as manutenções. Serve para quando o carro passa do pai para o filho.
- O texto da notificação inclui o dono quando há mais de um perfil: *"Troca de óleo do Gol (Ana) vence em 5 dias"*.

**Mudanças**
- [ ] `ProfilesViewModel` e telas de lista e edição de perfil.
- [ ] `MainViewModel` observa `activeProfileId` e faz `flatMapLatest` para `observeVehicles(profileId)`.
- [ ] Ação "Transferir para…" no `VehicleDetailsScreen`.
- [ ] `NotificationReceiver` / `NotificationUtils`: o extra da notificação passa a levar o nome do veículo e do perfil.

**Testes**
- [ ] Unitários:
  - trocar o perfil ativo troca a lista de veículos;
  - excluir o perfil ativo muda o ativo para outro;
  - excluir o único perfil é bloqueado;
  - a transferência some da lista do perfil de origem e aparece no destino;
  - o texto da notificação com um perfil e com vários;
  - o Início em "Todos" junta as pendências de todos os perfis e o filtro por perfil restringe a lista;
  - abrir uma pendência de outro perfil troca o perfil ativo.
- [ ] 📱 UI: criar o segundo perfil pelo seletor, alternar e conferir a lista; o diálogo de exclusão mostra a contagem.

**Validação**
- [ ] Portão mínimo verde.
- [ ] Manual: dois perfis com veículos → reiniciar o aparelho → as notificações dos **dois** perfis continuam agendadas.

**Commit:** `Adiciona multiplos perfis e transferencia de veiculo`

---

## Etapa 7 — Auto Backup

**Mudanças**
- [ ] `data_extraction_rules.xml` (Android 12+) e `backup_rules.xml` (≤ 11): **incluir** `database` (o arquivo do Room e seus `-wal`/`-shm`), `file` com path `images/` e o `datastore`. Excluir `cache` e `no_backup`. O comentário atual (sobre o token do Firebase) deixa de valer e deve ser reescrito.
- [ ] Usar `<include>` explícito para que um diretório novo não entre no backup sem querer.
- [ ] **Limite de 25 MB do Auto Backup:** usar o tamanho médio medido na etapa 3 (alvo ~150–300 KB por foto de veículo) e documentar quantas fotos cabem. Se o espaço apertar, reduzir o lado máximo, e não a qualidade abaixo de 70.
- [ ] Depois de uma restauração, reagendar os alarmes, porque eles não fazem parte do backup. O `enqueueReschedule` na abertura da `MainActivity` já cobre isso, então basta confirmar.

**Testes**
- Não há bom teste automatizado de backup. A validação é manual, com o roteiro abaixo registrado no PR.

**Validação**
```sh
adb shell bmgr enable true
adb shell bmgr transport com.android.localtransport/.LocalTransport
adb shell bmgr backupnow br.com.gabrielmorais.autocare
adb shell pm clear br.com.gabrielmorais.autocare          # apaga os dados locais
adb shell bmgr restore br.com.gabrielmorais.autocare       # restaura do transporte local
```
- [ ] Depois da restauração: perfis, veículos, manutenções e **fotos** de volta, e notificações reagendadas ao abrir o app.
- [ ] Repetir num aparelho com conta Google (transporte GMS): desinstalar → reinstalar → os dados voltam.

**Commit:** `Ativa o Auto Backup do banco e das fotos`

---

## Etapa 8 — Exportar / importar

**Formato do arquivo** `autocare-backup-AAAA-MM-DD.zip`:
```
manifest.json   { "format": "autocare-backup", "schemaVersion": 1,
                  "exportedAt": "...", "appVersion": "1.0.0",
                  "profiles": [ { ..., "vehicles": [ { ..., "maintenances": [ ... ] } ] } ] }
images/<uuid>.jpg
```

**Comportamento**
- Em Perfil → **Backup**:
  - "Exportar dados" abre `ActivityResultContracts.CreateDocument("application/zip")`, para que o usuário escolha o Drive, Downloads, WhatsApp etc.
  - "Importar dados" abre `OpenDocument`.
- Exporta **todos os perfis**.
- Importação por **mescla por id**: o que já existe com o mesmo UUID é atualizado e o resto é inserido. Nada que está no aparelho e não está no arquivo é apagado.
- **Prévia obrigatória** antes de aplicar, calculada lendo o arquivo sem gravar nada: *"Novos: 1 perfil, 2 veículos, 10 manutenções · Atualizados: 3 veículos, 30 manutenções. Importar?"*. Cancelar não deixa nenhum resíduo, nem no banco nem nas fotos.
- Tudo dentro de uma única transação Room. Se algo falhar, nada é aplicado e as fotos já extraídas são apagadas.
- Depois de importar: reagendar as notificações e rodar a varredura de órfãos.

**Segurança do importador** (o arquivo vem de fora e não é confiável)
- [ ] Rejeitar entradas com `..`, caminho absoluto ou fora de `manifest.json` / `images/*.jpg` (zip slip).
- [ ] Limites: tamanho total descompactado, número de entradas e tamanho de cada imagem (contra zip bomb).
- [ ] Cada imagem importada precisa ser um JPEG válido (marcador `FF D8 FF` e decodificável). Se passar do lado máximo da política da etapa 3, é **recomprimida** pelo `ImageCompressor` antes de ir para `filesDir`, para que um arquivo vindo de fora não fure a política de espaço.
- [ ] Validar `format` e `schemaVersion`. Uma versão **maior** que a suportada gera a mensagem "Atualize o app para importar este arquivo".
- [ ] Validar campos obrigatórios e tipos. Um registro inválido aborta a importação com uma mensagem clara.
- [ ] O JSON é mapeado à mão com `org.json`: nenhuma dependência nova e nenhuma reflexão (seguro para o R8).

**Mudanças**
- [ ] `data/backup/`: `BackupSerializer` (domínio ↔ JSON), `BackupExporter` (zip) e `BackupImporter` (validação + aplicação).
- [ ] UI de backup e `BackupViewModel` com estados de progresso, sucesso e erro.

**Testes**
- [ ] Unitários (JVM, `java.util.zip` + `org.json` reais):
  - ida e volta: exportar → importar num banco vazio → dados iguais;
  - mescla: importar sobre dados existentes atualiza pelo id e não duplica;
  - mescla: registros do aparelho que não estão no arquivo continuam intactos;
  - a prévia conta corretamente os novos e os atualizados e não grava nada;
  - cancelar depois da prévia não deixa nenhum arquivo em `images/`;
  - zip slip (`../../x`) é rejeitado;
  - um `schemaVersion` maior é rejeitado com a mensagem certa;
  - um JSON truncado ou campo obrigatório ausente aborta sem aplicar nada;
  - limites de tamanho e de quantidade de entradas.
- [ ] 📱 Instrumentado: exportar com fotos → limpar o banco → importar → as fotos abrem.

**Validação**
- [ ] Portão mínimo e portão de release verdes (o release confirma que o R8 não quebrou o serializador).
- [ ] Manual: exportar no emulador A → enviar o arquivo → importar no emulador B.

**Commit:** `Adiciona exportar e importar dados em arquivo zip`

---

## Etapa 9 — `targetSdk` exigido pela Play + edge-to-edge

**Por quê:** a Play bloqueia o upload de apps abaixo do `targetSdk` mínimo vigente. **Confirme o valor no Play Console** antes de começar (35 desde agosto de 2025; provavelmente 36 a partir de agosto de 2026).

**Mudanças**
- [ ] `targetSdk` → valor exigido.
- [ ] `enableEdgeToEdge()` na `MainActivity`. Revisar os insets em todas as telas: `Scaffold` (já usado em `AutoCareApp`), onboarding, diálogos, formulário com teclado aberto (`imePadding`) e a `CropImageActivity` da biblioteca.
- [ ] Com o target 36, o *predictive back* fica ativo por padrão. Conferir os `BackHandler` e o comportamento do NavHost.
- [ ] Revisar o pedido de `POST_NOTIFICATIONS` e da câmera no novo target.
- [ ] Remover `tools:targetApi="31"` do manifesto, se não for mais necessário.

**Testes**
- [ ] 📱 Rodar a suíte instrumentada num emulador com a API do novo target (incluir na matriz do `instrumented-tests.yml`).

**Validação**
- [ ] Manual, no emulador com a API nova, modo claro e escuro, navegação por gestos e por 3 botões: nenhum conteúdo sob a status bar ou a barra de navegação, e o FAB e a barra inferior não são cobertos.

**Commit:** `Sobe targetSdk e adota edge-to-edge`

---

## Etapa 10 — Anúncios (AdMob + UMP)

**Mudanças**
- [ ] Dependências: `play-services-ads` e `user-messaging-platform`.
- [ ] IDs por build type, via `manifestPlaceholders` e `buildConfigField`:
  - **debug:** IDs de teste oficiais do Google (app `ca-app-pub-3940256099942544~3347511713`, banner adaptativo `…/9214589741`, intersticial `…/1033173712`);
  - **release:** variáveis de ambiente `ADMOB_APP_ID`, `ADMOB_BANNER_ID` e `ADMOB_INTERSTITIAL_ID`, vindas de secrets no CI. **O build de release falha** se alguma estiver vazia, para nunca publicar com ID de teste nem sem ID.
- [ ] Manifesto: `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="${admobAppId}"/>`.
- [ ] `AdsConsentManager` (UMP):
  - `requestConsentInfoUpdate` → `loadAndShowConsentFormIfRequired` na abertura;
  - `MobileAds.initialize` **só depois** de `canRequestAds()`, fora da main thread.
- [ ] Item **"Privacidade e anúncios"** em Perfil, visível quando `privacyOptionsRequirementStatus == REQUIRED` (exigência do UMP).
- [ ] Abstração `AdsController` (interface) com implementação real e uma fake para os testes, para que nenhum teste dependa de rede ou do SDK.
- [ ] **Posicionamento:**
  - banner adaptativo âncora no rodapé de **Veículos** e do **Detalhe do veículo**, acima da barra inferior, sem cobrir conteúdo;
  - **nenhum anúncio** no formulário de manutenção, no onboarding ou nas telas de backup, para evitar cliques acidentais (política da Play);
  - intersticial **só depois de salvar uma manutenção**, regido por uma `InterstitialPolicy` em Kotlin puro: nunca na primeira sessão, no mínimo 3 minutos entre exibições, no máximo N por dia e pré-carregado (nunca bloqueia a navegação).
- [ ] R8: o SDK já traz as regras de consumidor. Mesmo assim, confirmar com o portão de release.
- [ ] Data safety (preparação): listar o que o SDK coleta, para usar na etapa 11.

**Testes**
- [ ] Unitários de `InterstitialPolicy` com relógio injetado: primeira sessão, intervalo mínimo, limite diário e virada do dia.
- [ ] Unitário: o build de release sem IDs falha (testar a função de leitura e validação das variáveis).
- [ ] 📱 UI: com o `AdsController` fake, o banner aparece só nas telas previstas e salvar uma manutenção chama `maybeShowInterstitial` uma vez.

**Validação**
- [ ] Manual no debug: banner e intersticial **de teste** aparecendo.
- [ ] Formulário de consentimento forçado com `ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA` → aceitar e recusar → o app continua utilizável nos dois casos.
- [ ] O APK Analyzer não aponta bibliotecas nativas incompatíveis com páginas de 16 KB.
- [ ] Portão de release verde.
- [ ] ⚠️ Nunca clicar em anúncio real do próprio app.

**Commit:** `Adiciona anuncios AdMob com consentimento UMP`

---

## Etapa 11 — Release, site e Play Console

**Site (política de privacidade + app-ads.txt)**
- [ ] O `app-ads.txt` precisa estar na **raiz do domínio** cadastrado como site do desenvolvedor. Uma página de projeto (`usuario.github.io/AutoCare/`) **não é raiz**. Use o repositório de site de usuário (`<usuario>.github.io`) ou um domínio próprio.
- [ ] A política de privacidade (pt-BR) deve explicar:
  - os dados ficam no aparelho;
  - o backup é feito pelo Google Drive da própria pessoa (Auto Backup) ou por arquivo exportado por ela;
  - o AdMob coleta ID de publicidade, localização aproximada, interações e diagnóstico;
  - o consentimento e como revogá-lo;
  - o contato do desenvolvedor.
- [ ] Link para a política dentro do app (Perfil → Sobre).

**CI / release**
- [ ] `release.yml`: adicionar os secrets do AdMob e conferir que os antigos foram removidos na etapa 5.
- [ ] Manter o `mapping.txt` e **também** enviá-lo ao Play Console junto com cada AAB.
- [ ] Primeira versão: tag `v1.0.0`.
- [ ] Guardar o keystore e as senhas fora do GitHub. Na primeira publicação, ativar o Play App Signing (a chave do CI vira a chave de upload).

**Play Console**
- [ ] Ficha da loja: ícone 512×512, feature graphic 1024×500, screenshots de celular (com dois perfis, para destacar o recurso) e descrições curta e completa.
- [ ] Segurança dos dados:
  - dados do app (perfis, veículos, fotos): **não coletados**, pois não saem do aparelho;
  - SDK AdMob: ID do dispositivo, localização aproximada, interações e diagnóstico, **coletados e compartilhados** para publicidade, analytics e prevenção de fraude;
  - criptografia em trânsito: sim.
- [ ] Contém anúncios: **sim**. ID de publicidade: **sim** (uso: publicidade).
- [ ] Classificação de conteúdo (questionário). Público-alvo **18+**, para ficar fora da política Families.
- [ ] Acesso ao app: "todas as funcionalidades disponíveis sem restrições" (não há mais login).
- [ ] **Conta de organização:** obter o número D-U-N-S (gratuito, mas leva alguns dias úteis; **iniciar já**), concluir a verificação da organização e do site no Console. O nome da organização aparece como desenvolvedor na loja.
- [ ] Teste interno (ou fechado, com testadores próprios) → produção. A conta de organização **não** tem a exigência de 12 testadores por 14 dias, mas um teste interno curto num aparelho real antes da produção continua recomendado.
- [ ] AdMob: vincular o app depois de publicado, para a revisão e a verificação do `app-ads.txt`.

**Validação**
- [ ] O AAB assinado pelo `release.yml` é aceito no teste interno sem avisos bloqueantes (target SDK, 16 KB, permissões).
- [ ] Instalação limpa pela Play num aparelho real: onboarding → veículo → manutenção → notificação → exportar → anúncios de produção aparecendo (sem clicar).

**Commit:** `Prepara a release 1.0.0 para a Play Store`

---

## Para depois da 1.0

- **"Remover anúncios"** (compra única pelo Play Billing). A `AdsController` já nasce preparada para ser desligada por uma flag.
