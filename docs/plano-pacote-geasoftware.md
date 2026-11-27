# Plano: pacote `br.com.gabrielmorais.autocare` → `br.com.geasoftware.autocare` ✅

*Criado em 27/11/2026.* O app vai ser publicado pela GeA Software LTDA, como o Retro 99
(`br.com.geasoftware.retro99`). O `applicationId` é a identidade do app na Play e **não pode mudar depois
da primeira publicação**, então a troca acontece agora, antes do teste interno e antes de cadastrar o app
no AdMob. Valem as [regras gerais](plano-publicacao.md#regras-gerais-de-cada-etapa) do plano de publicação.

## Decisão: renomear tudo, não só o `applicationId`

| Opção | O que muda | Por que não / por que sim |
| --- | --- | --- |
| Só `applicationId` | 1 linha no `build.gradle` | O código continuaria em `br.com.gabrielmorais`, o `namespace` (pacote do `R` e do `BuildConfig`) também, e o nome antigo ficaria para sempre nas stack traces e no mapping do R8. |
| **`applicationId` + `namespace` + pacotes Kotlin** | 117 arquivos `.kt`, mas só linhas `package`/`import` | Uma troca mecânica, feita uma vez e verificada por `grep`. O app fica com um nome só. **Escolhida.** |

## O que precisa mudar (levantado em 27/11/2026 sobre `main` = `03f6142`)

**Build**
- `app/build.gradle`: `namespace` e `applicationId`.

**Código** (`app/src/{main,test,androidTest}/java/br/com/gabrielmorais/autocare/` → `…/br/com/geasoftware/autocare/`)
- 117 arquivos: 82 em `main`, 24 em `test`, 11 em `androidTest`. Quase tudo são linhas `package` e `import`, inclusive `import br.com.gabrielmorais.autocare.R` e `.BuildConfig`, que seguem o `namespace`.
- Literais fora de `package`/`import`:
  - `ExampleInstrumentedTest.kt`: compara `appContext.packageName` com o nome antigo;
  - `Entities.kt`: KDoc com o nome qualificado de `Maintenance`.
- O manifesto usa nomes relativos (`.AutoCareApplication`, `.ui.activities…`) e não tem `${applicationId}` nem `authorities`: **não muda**.

**Room**
- `app/schemas/br.com.gabrielmorais.autocare.data.local.AutoCareDatabase/1.json`: a pasta tem o nome qualificado da classe do banco. Vai com `git mv` para `app/schemas/br.com.geasoftware.autocare.data.local.AutoCareDatabase/`, para o histórico de esquemas continuar valendo nas migrações futuras. O conteúdo do JSON não cita o pacote.

**Documentação e scripts**
- `README.md`: links para `ImageCompressor.kt` e `PhotoHousekeeping.kt`, e o comando do runner de testes (`br.com.gabrielmorais.autocare.test/…`).
- `docs/plano-publicacao.md`: comandos `adb shell run-as`, `bmgr` e `pm clear`.
- `docs/plano-sobras.md`: link para `MaintenanceFormScreen.kt`.
- `install-app.sh` (só na branch `chore/install-app-script`, ainda sem PR): `am start -n "br.com.gabrielmorais.autocare/…"`.
- Na branch da etapa 11: `docs/play-store.md` e o link da Play em `site/README.md` (que sai junto com a pasta `site/`, ver "Ordem").

**O que não muda**
- A página no ar (`geasoftware.com.br/autocare/privacidade`) não cita o pacote.
- `app-ads.txt`, canal de notificação, nomes do DataStore e das SharedPreferences, formato do backup em `.zip`: nada disso usa o pacote.
- Regras do R8 (`proguard-rules.pro`): não citam o pacote do app.

## Efeitos fora do repositório

- **Aparelhos com o app antigo:** para o Android, `br.com.geasoftware.autocare` é outro app. O antigo continua instalado, com os dados dele, ao lado do novo. Como o app nunca foi publicado, isso só afeta os aparelhos de teste: exportar os dados no app antigo (Perfil → Backup), instalar o novo, importar no primeiro acesso ("Restaurar de um arquivo") e desinstalar o antigo. No emulador, basta desinstalar.
- **Auto Backup:** o backup na conta Google é por pacote; o do nome antigo não volta no app novo. Sem usuários reais, nada se perde.
- **AdMob e Play Console:** ainda não têm o app cadastrado. Cadastrar já com `br.com.geasoftware.autocare`.
- **Site:** avisar o projeto `gahmorais/geasoftware` do pacote novo, para o link da Play no card da home (`https://play.google.com/store/apps/details?id=br.com.geasoftware.autocare`).

## Ordem

A troca mexe em 117 arquivos, e qualquer branch aberta que toque neles entra em conflito. Por isso ela vem
**depois** de fechar o que está aberto e **antes** da tag `v1.0.0`:

1. Merge do PR #15 (`fix/veiculo-sem-distancia`).
2. Etapa 11: trocar a URL da política em `app/build.gradle` por `https://geasoftware.com.br/autocare/privacidade`, apagar `site/` (a página vive no repositório do site), abrir o PR e fazer o merge.
3. `chore/install-app-script`: abrir o PR e fazer o merge, ou descartar a branch. Se entrar depois da troca, corrigir o pacote no script antes.
4. Esta troca, numa branch a partir de `main` atualizada (esta, `chore/pacote-geasoftware`, com rebase).

## Passos da troca

- [x] `git mv app/src/{main,test,androidTest}/java/br/com/gabrielmorais app/src/{main,test,androidTest}/java/br/com/geasoftware` (uma por source set, para o git seguir o histórico de cada arquivo como renomeação).
- [x] `git mv` da pasta do esquema do Room.
- [x] Substituir `br.com.gabrielmorais.autocare` por `br.com.geasoftware.autocare` em todo o repositório, menos `build/` e `.git/`. Isso cobre `package`, `import`, os dois literais, o `build.gradle` e as docs.
- [x] Substituir os caminhos `br/com/gabrielmorais/autocare` nos links das docs.
- [x] `git grep gabrielmorais`: zero ocorrências nos arquivos versionados (121 arquivos alterados). O nome antigo só aparece neste plano, de propósito.
- [x] `./gradlew clean`, porque o `build/` antigo tem as classes geradas no pacote velho.

## Testes e validação

- [x] Portão mínimo e de release verdes (`testDebugUnitTest lintDebug assembleDebug lintRelease assembleRelease`), 175 testes na JVM.
- [x] `ExampleInstrumentedTest` passa a conferir `br.com.geasoftware.autocare`.
- [x] 📱 `connectedDebugAndroidTest` verde no emulador API 36: 57 testes, todos em `br.com.geasoftware.autocare`.
- [x] O Room acha o esquema na pasta nova: depois do build, `app/schemas/` só tem a pasta movida e o git vê `1.json` como renomeado, sem alteração.
- [x] `aapt2 dump badging` do APK de release mostra `package: name='br.com.geasoftware.autocare'`.
- [x] Manual no emulador: instalar o novo, primeiro acesso → "Restaurar de um arquivo" → o backup entra e o Início mostra os dados; Perfil → Sobre → "Política de privacidade" abre o Chrome em `https://geasoftware.com.br/autocare/privacidade`. O app antigo não estava mais no emulador, então a exportação a partir dele não foi testada aqui; exportar e importar não mudaram neste commit.

**Commit:** `Troca o pacote para br.com.geasoftware.autocare`

## Depois do merge

- [ ] Atualizar o `docs/play-store.md` (ficha) se ainda citar o pacote antigo.
- [ ] Mensagem para o inbox de `gahmorais/geasoftware` com o pacote novo e o link futuro da Play.
- [ ] Cadastrar o app no AdMob e no Play Console com `br.com.geasoftware.autocare`.
- [ ] Nos aparelhos de teste: exportar do app antigo, importar no novo e desinstalar o antigo.
