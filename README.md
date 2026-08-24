## Aplicativo AutoCare
### Uma maneira simples de fazer o controle de manutenção do seu veículo

![image](./imgs/profile.png)
![image](./imgs/cars.png)
![image](./imgs/account.png)
![image](./imgs/car-details.png)
![image](./imgs/add_maintenance.png)

## Configuração

O projeto depende de um projeto Firebase (Authentication e Realtime Database).
O armazenamento de imagens **não** usa Firebase Storage — ver *Imagens* abaixo.

1. Crie o projeto no [Firebase Console](https://console.firebase.google.com/) e registre o app
   com o applicationId `br.com.gabrielmorais.autocare`.
2. Baixe o `google-services.json` e coloque-o em `app/google-services.json`.
   **Esse arquivo não é versionado** — sem ele o build falha no plugin `com.google.gms.google-services`.
3. Habilite o provedor *E-mail/senha* em Authentication.
4. Publique as regras de segurança versionadas neste repositório:

   ```sh
   npm install -g firebase-tools   # se ainda não tiver
   firebase login
   firebase deploy --only database
   ```

   O [`firebase.json`](./firebase.json) aponta o serviço para o arquivo de regras e o
   [`.firebaserc`](./.firebaserc) fixa o projeto padrão — sem eles o CLI responde
   *"Not in a Firebase app directory"*. Para apontar para outro projeto: `firebase use --add`.

   [`database.rules.json`](./database.rules.json) restringe `Usuarios/{uid}` e `vehicles/{uid}`
   ao próprio usuário autenticado e deixa `lista-servicos` como somente leitura.

   > ⚠️ Conceder `".read": "auth != null"` **no nó pai** (`Usuarios`, `vehicles`) expõe os dados
   > de todos os usuários: as regras cascateiam para baixo e não podem ser revogadas no filho.
   > Como o cadastro é self-service, qualquer pessoa cria uma conta e lê o banco inteiro via
   > REST. A permissão precisa ficar no nível `$uid`, como no arquivo versionado.

   **Compatibilidade com versões já instaladas:** o escopo por `$uid` não quebra clientes
   antigos — todo acesso do app sempre foi em ou abaixo do próprio uid. As regras
   propositalmente **não** incluem `.validate`: elas seriam restrições novas sobre a escrita,
   e versões anteriores à correção de tratamento de erro lançam exceção de dentro dos
   callbacks do Firebase, transformando uma rejeição em crash em vez de mensagem.
   Se quiser adicionar `.validate` depois, confirme antes que todo registro em
   `Usuarios/{uid}` tem `id` presente e igual ao uid, e valide no *Rules Playground*.

5. Popule o nó `lista-servicos` do Realtime Database com os tipos de serviço:

   ```json
   {
     "lista-servicos": {
       "0": { "name": "Troca de óleo", "mileageChange": 10000, "mustBeDoneBefore": 12 }
     }
   }
   ```

### Imagens (Cloudinary)

As fotos de perfil e de veículo são hospedadas no [Cloudinary](https://cloudinary.com/) —
o Firebase encerrou o tier gratuito de Storage. O upload é **não assinado**, via
*upload preset*, porque assinar exigiria o `api_secret` no cliente (inaceitável) ou um
backend (o projeto não tem).

Declare as credenciais em `local.properties` — arquivo não versionado:

```properties
cloudinary.cloudName=seu-cloud-name
cloudinary.uploadPreset=seu-preset
```

Sem elas o app compila, mas o upload falha com mensagem explícita apontando para cá.

O preset precisa ser criado como **Unsigned** no console, com estas restrições:

| Configuração | Valor | Por quê |
|---|---|---|
| Folder | `autocare` | Escopo fixo |
| Use filename / Unique filename | `false` / auto | **Ver aviso abaixo** |
| Allowed formats | `jpg, png, webp` | Impede upload arbitrário |
| Incoming transformation | `c_limit,w_1600,h_1600` | Reduz custo de storage |

> **Limite de tamanho:** não é configurável por preset no plano gratuito — vale o limite
> da conta. O `c_limit` acima já contém o custo de storage (corta as dimensões antes de
> armazenar), e o app rejeita arquivos acima de 5 MB antes de enviar
> (`CloudinaryUploadApi.DEFAULT_MAX_FILE_SIZE_BYTES`). Para conferir o que o preset tem
> gravado de fato: `curl -u API_KEY:API_SECRET https://api.cloudinary.com/v1_1/<cloud>/upload_presets/<preset>`.

> ⚠️ **O `public_id` deve ser gerado pelo Cloudinary.** Se o preset permitir que o cliente
> defina o `public_id`, um atacante pode sobrescrever a imagem de outro usuário passando o
> id dela. Por isso o app não envia esse campo e guarda a `secure_url` retornada.

**Limitações conhecidas deste modelo:**

- O `cloud_name` e o preset são extraíveis do APK — quem fizer isso pode subir imagens
  nesta conta. As restrições do preset limitam o estrago, mas não eliminam o vetor.
- **Não há como deletar** pelo app: a Admin API é assinada. Trocar a foto de perfil ou
  excluir um veículo deixa o asset órfão.

Ambos deixam de valer se algum dia houver um backend para assinar as requisições — nesse
caso só a implementação de `ImageUploader` muda.

### Limpando as fotos antigas do Firebase Storage

URLs de foto gravadas antes da migração apontam para `firebasestorage.googleapis.com` e
nunca mais vão resolver. Elas não quebram o app — cai no placeholder —, mas cada render
dispara uma requisição condenada a falhar. [`scripts/clear-legacy-photos.js`](./scripts/clear-legacy-photos.js)
anula esses campos em `Usuarios/{uid}/photo` e `vehicles/{uid}/{id}/photo`.

Precisa de uma chave de service account: *Firebase Console → Configurações do projeto →
Contas de serviço → Gerar nova chave privada*.

```sh
cd scripts
npm install
export GOOGLE_APPLICATION_CREDENTIALS=/caminho/da/service-account.json
export FIREBASE_DATABASE_URL=https://<project-id>-default-rtdb.firebaseio.com

npm run clear-legacy-photos          # simulação: lista o que seria apagado
npm run clear-legacy-photos:apply    # grava
```

O script roda em **modo simulação por padrão** — só apaga com `--apply`.

> ⚠️ A chave de service account dá acesso administrativo total ao projeto e ignora as
> regras de segurança. O `.gitignore` cobre `service-account*.json`, mas confira antes de
> commitar.

É uma tarefa de uma vez só: fotos novas já nascem no Cloudinary.

## Testes

```sh
./gradlew testDebugUnitTest      # 90 testes JVM
```

`ImageCompressor` só tem cobertura instrumentada: `BitmapFactory` e `Bitmap` são stubs no
android.jar de teste e qualquer asserção passaria sem exercitar nada.

```sh
./gradlew connectedDebugAndroidTest
```

> Se o aparelho estiver pareado **sem fio** (serial no formato `adb-XXXX._adb-tls-connect._tcp`),
> o AGP 8.0.2 coleta 0 testes e falha a task sem executar nada. Conecte por USB, ou rode a
> instrumentação direto:
>
> ```sh
> ./gradlew installDebug installDebugAndroidTest
> adb shell wm dismiss-keyguard   # senão a Activity para em STOPPED
> adb shell am instrument -w \
>   br.com.gabrielmorais.autocare.test/androidx.test.runner.AndroidJUnitRunner
> ```

O build requer **JDK 17** (AGP 8.0.2).

## CI/CD

Quatro workflows em [`.github/workflows/`](.github/workflows/). O setup comum — JDK 17, cache do
Gradle e os arquivos não versionados que o build exige — está na action composta
[`.github/actions/setup-android`](.github/actions/setup-android/action.yml).

| Workflow | Dispara em | O que faz |
| --- | --- | --- |
| [`ci.yml`](.github/workflows/ci.yml) | push na `main`, todo PR | `testDebugUnitTest`, `lintDebug`, `assembleDebug` |
| [`instrumented-tests.yml`](.github/workflows/instrumented-tests.yml) | 03:00 BRT, manual | `connectedDebugAndroidTest` em emulador, API 28 e 34 |
| [`release.yml`](.github/workflows/release.yml) | tag `v*`, manual | APK + AAB assinados e GitHub Release |
| [`deploy-database-rules.yml`](.github/workflows/deploy-database-rules.yml) | push na `main` que toque nas regras | `firebase deploy --only database` |

### Secrets

Em *Settings → Secrets and variables → Actions*. Nenhum é obrigatório para o `ci.yml` passar:
sem `GOOGLE_SERVICES_JSON_BASE64` a action escreve um `google-services.json` **placeholder**, que
só existe para o plugin `com.google.gms.google-services` não abortar o build. Com ele o app compila
e os testes JVM rodam, mas nada conecta no Firebase — é o que mantém PR de fork verde.

| Secret | Usado por | Como obter |
| --- | --- | --- |
| `GOOGLE_SERVICES_JSON_BASE64` | todos | `base64 -w0 app/google-services.json` |
| `RELEASE_KEYSTORE_BASE64` | release | `base64 -w0 release.jks` |
| `RELEASE_KEYSTORE_PASSWORD` | release | senha do keystore |
| `RELEASE_KEY_ALIAS` | release | alias da chave |
| `RELEASE_KEY_PASSWORD` | release | senha da chave |
| `CLOUDINARY_CLOUD_NAME` | todos | ver *Imagens* acima |
| `CLOUDINARY_UPLOAD_PRESET` | todos | ver *Imagens* acima |
| `FIREBASE_SERVICE_ACCOUNT` | deploy das regras | JSON da service account, colado inteiro |

> ⚠️ `FIREBASE_SERVICE_ACCOUNT` é a mesma chave administrativa descrita em *Limpando as fotos
> antigas*: ignora as regras de segurança. O `deploy-database-rules.yml` roda no environment
> `production` — dá para exigir aprovação manual em *Settings → Environments* se o deploy
> automático a cada push incomodar.

### Publicando uma versão

A tag é a fonte da versão. `versionName` sai dela e `versionCode` do número da execução:

```sh
git tag v1.1.0 && git push origin v1.1.0
```

O workflow roda testes e `lintRelease` antes de assinar — a tag pode apontar para um commit que
nunca passou no CI. Depois de assinar, confere com `apksigner verify` antes de publicar.

Para isso funcionar, [`app/build.gradle`](app/build.gradle) lê assinatura e versão de variáveis de
ambiente (`RELEASE_KEYSTORE_PATH`, `VERSION_NAME`, `VERSION_CODE`). **Sem elas o build local é
exatamente o de antes**: versão `1`/`1.0` e `app-release-unsigned.apk`.

O `mapping.txt` do R8 vai só para os artefatos da execução (90 dias), fora da Release — é anexo
público e serve para desofuscar o APK. Sem ele, as stack traces daquela versão ficam ilegíveis
para sempre, então baixe e guarde antes de expirar.

## Débito técnico

### Transições entre destinos do NavHost

O redesenho previa transições M3 ao navegar (deslize ao abrir o detalhe do veículo, retorno na
direção inversa). Ficou de fora: `NavHost` só aceita `enterTransition` / `exitTransition` /
`popEnterTransition` / `popExitTransition` a partir do **navigation-compose 2.7**, e o projeto está
no 2.6 porque a cadeia inteira está presa a uma combinação:

```
kotlinCompilerExtensionVersion 1.4.2  ->  Kotlin 1.8.10  ->  compose-bom 2023.06.01
                                                          ->  compose-ui 1.4.3 / material3 1.1.1
```

O 2.7 exige Compose 1.5, que exige o compilador 1.5.x, que exige Kotlin 1.9. Ou seja: **não é uma
troca de versão da navegação, é subir Kotlin, o compilador do Compose e a BOM de uma vez.** Por isso
foi deixado para uma mudança própria, em vez de embutido no commit de polimento.

Enquanto isso o 2.6 aplica *fade-through* entre destinos, que é o padrão do M3 para troca de
superfície — o que falta é só o deslize direcional.

**Ao fazer o upgrade:** as durações e easings já existem em
[`ui/theme/Motion.kt`](app/src/main/java/br/com/gabrielmorais/autocare/ui/theme/Motion.kt)
(`Motion.SHORT`, `Motion.MEDIUM`, `EmphasizedDecelerate`, `EmphasizedAccelerate`), e o
`LocalReducedMotion` do mesmo arquivo precisa ser respeitado nas quatro transições — o Compose não
honra `ANIMATOR_DURATION_SCALE` sozinho, então sem essa checagem o app anima para quem desligou
animações nas opções de acessibilidade. O ponto de aplicação é o `NavHost` em
[`ui/navigation/AutoCareApp.kt`](app/src/main/java/br/com/gabrielmorais/autocare/ui/navigation/AutoCareApp.kt),
onde há um comentário marcando o lugar.

