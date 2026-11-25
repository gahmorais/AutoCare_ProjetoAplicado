# Plano: sobras conhecidas antes da release

*Criado em 25/11/2026.* Corrige as quatro "Sobras conhecidas" listadas em [plano-publicacao.md](plano-publicacao.md#andamento). Cada uma é pequena e independente, por isso vira uma etapa curta, com branch e PR próprios. Valem as mesmas [regras gerais](plano-publicacao.md#regras-gerais-de-cada-etapa) do plano de publicação: portão mínimo antes de cada commit, portão de release onde indicado e 📱 para instrumentados.

A ordem vai do mais grave para o mais simples. A S4 fica por último porque as etapas anteriores mexem em strings e podem passar a usar ou deixar sem uso alguns recursos.

| Etapa | Branch | Gravidade |
|---|---|---|
| S1 — Descrição fora do catálogo ✅ | `fix/sobra-1-descricao-fora-do-catalogo` | perda de dado sem aviso |
| S2 — "Adicionar veículo" com filtro ✅ | `fix/sobra-2-adicionar-veiculo-filtro` | veículo vai para o perfil errado |
| S3 — Restaurar no onboarding | `feat/sobra-3-restaurar-onboarding` | perfil vazio sobrando |
| S4 — Recursos sem uso | `chore/sobra-4-recursos-sem-uso` | só limpeza |

---

## S1 — Descrição fora do catálogo no formulário de manutenção ✅

**Problema.** Em [MaintenanceFormScreen.kt](../app/src/main/java/br/com/gabrielmorais/autocare/ui/activities/add_maintenance_screen/MaintenanceFormScreen.kt), `selectedIndex` começa em `options.indexOfFirst { it.name == editing?.description }.coerceAtLeast(0)`. Quando a descrição não está no catálogo (`assets/services.json`), o formulário cai em "Aditivo do radiador". Ao salvar, esse nome substitui a descrição original. Os dois `LaunchedEffect` de previsão também passam a usar a quilometragem e o prazo desse serviço errado.

Hoje isso só acontece com um backup importado que foi montado fora do app ou que veio de uma versão com outro catálogo. Exemplo reproduzido: uma manutenção importada "Troca de óleo", sendo que o catálogo tem "Óleo de motor".

**Comportamento esperado**
- Ao editar uma manutenção cuja descrição não está no catálogo, essa descrição aparece **selecionada** e é a primeira opção da lista, marcada como "(fora do catálogo)" no menu. Salvar sem mexer no serviço mantém a descrição original.
- Enquanto essa opção estiver selecionada, as previsões **não são recalculadas**, porque ela não tem intervalo de quilometragem nem prazo. A previsão já gravada continua valendo, e o usuário pode editá-la à mão como hoje.
- Se o usuário escolher um serviço do catálogo, o comportamento atual volta: as previsões são recalculadas.
- Criar uma manutenção nova continua igual, começando no primeiro serviço do catálogo.
- Uma descrição nula ou em branco continua caindo no primeiro serviço, porque não há dado a perder.

**Mudanças**
- [x] `ServiceOption` ganha `mileageChange` e `mustBeDoneBefore` anuláveis e a propriedade `fromCatalog`. A opção fora do catálogo é montada com esses campos nulos.
- [x] Extrair para uma função pura, testável na JVM, a montagem da lista e o índice inicial, em `ServiceChoices.kt`: `catalogOptions(services)` e `serviceChoices(catalog, editingDescription)`.
- [x] Os dois `LaunchedEffect` de previsão saem cedo quando a opção selecionada não tem intervalo.
- [x] Rótulo "(fora do catálogo)" em `strings.xml`, e não fixo no código.

**Testes**
- [x] Unitários da função extraída: descrição no catálogo → índice dela; fora do catálogo → opção extra no índice 0 com o nome original; nula ou em branco → índice 0 sem opção extra; criação → lista igual ao catálogo.
- [x] `MaintenanceFormScreenTest`: abrir em edição uma manutenção "Troca de óleo", salvar sem mexer → a manutenção salva tem `description == "Troca de óleo"` e a mesma previsão de antes.
- [x] `MaintenanceFormScreenTest`: na mesma situação, escolher "Óleo de motor" → a previsão é recalculada.

**Validação**
- [x] Portão mínimo verde.
- [x] 📱 `connectedDebugAndroidTest` verde no emulador API 36 (47 testes).
- [x] Manual no emulador: importar um backup com "Troca de óleo", abrir a manutenção → o campo mostra "Troca de óleo (fora do catálogo)" e as previsões gravadas; o menu lista essa opção e os 13 serviços; salvar sem mexer mantém descrição, 57.000 km e 10/08/2026.

**Ao terminar (feito):** tirar o item das sobras em `plano-publicacao.md` e apagar a memória `bug-formulario-descricao-fora-do-catalogo`.

**Commit:** `Mantém a descrição fora do catálogo ao editar uma manutenção`

---

## S2 — "Adicionar veículo" do Início com o filtro em outro perfil ✅

**Problema.** No Início, com o filtro num perfil que não é o ativo e esse perfil sem veículos, o estado vazio mostra "Adicionar veículo". O botão leva para a aba Veículos, e `MainViewModel.saveVehicle` sempre grava o veículo com `profileId` do perfil **ativo**. O usuário filtrou "Maria", tocou em adicionar e o carro foi parar no perfil "João".

**Correção proposta.** Se houver filtro, o botão primeiro torna ativo o perfil filtrado e só depois navega. A aba Veículos já mostra os veículos do perfil ativo, então a pessoa vê que está no perfil certo, e o seletor de perfil no topo mostra a troca. Isso evita passar o dono por argumento de navegação e não muda `saveVehicle`.

**Mudanças**
- [x] `MainViewModel.prepareAddVehicle()`: se `homeFilter` não for nulo e for diferente do ativo, chama a mesma lógica de `selectProfile`.
- [x] `HomeScreen`: o `onAction` do estado vazio chama `viewModel.prepareAddVehicle()` e depois `onAddVehicle()`.

**Testes**
- [x] `MainViewModelTest`: dois perfis, ativo A, filtro B → `prepareAddVehicle()` → o perfil ativo passa a ser B; `saveVehicle` em seguida grava com `profileId == B`.
- [x] `MainViewModelTest`: sem filtro, ou com filtro igual ao ativo → o perfil ativo não muda.

**Validação**
- [x] Portão mínimo verde.
- [x] Manual no emulador: perfis "Importado" (ativo) e "Teste" (sem carro); filtrar "Teste" → "Adicionar veículo" abre Veículos já em "Teste" → salvar "Uno do Teste" → no Início, o filtro "Teste" passa a ter o carro e "Importado" continua só com o dele.

**Commit:** `Cria o veículo no perfil filtrado ao adicionar pelo Início`

---

## S3 — "Restaurar de um arquivo" no onboarding

**Problema.** A importação só existe em Perfil → Backup. Num aparelho novo sem Auto Backup, a pessoa precisa criar um perfil no onboarding para chegar à importação. Depois de importar, esse perfil fica sobrando, vazio.

**Comportamento esperado**
- A tela "Como você se chama?" ganha um botão secundário **"Restaurar de um arquivo"**, abaixo do principal.
- O fluxo é o mesmo do Perfil: `OpenDocument` → prévia obrigatória → confirmar ou cancelar. Cancelar volta ao onboarding sem resíduo.
- Depois de importar, o app vai direto para o Início sem criar perfil. Isso já sai de graça: `MainViewModel.startState` passa a `READY` quando a lista de perfis deixa de ser vazia. Como o DataStore ainda não tem perfil ativo, `activeProfile` cai no primeiro perfil.
- Um arquivo sem perfis, ou que dá "Nada a importar", mantém o onboarding e mostra a mensagem atual.
- Notificações e varredura de órfãos rodam como hoje, pelo `ImportSideEffects`.

**Mudanças**
- [ ] Separar de `BackupSection` a parte de importar (launcher `OpenDocument`, diálogo de prévia, progresso e resultado) num composable reutilizável, por exemplo `ImportBackupFlow(viewModel, trigger)`. A tela de Perfil passa a usar esse composable, sem mudar o que o usuário vê.
- [ ] `OnboardingScreen` recebe um slot `restoreAction: @Composable () -> Unit`, como o `backupSection` da tela de Perfil, para continuar testável sem Koin. O `AutoCareApp` passa o `ImportBackupFlow` com `koinViewModel()`.
- [ ] String "Restaurar de um arquivo".

**Testes**
- [ ] `OnboardingScreenTest`: o botão aparece e aciona o slot; o fluxo de criar perfil continua igual.
- [ ] `ProfileScreenTest` e `BackupViewModelTest` continuam verdes depois da extração, que é a rede de segurança dela.
- [ ] `MainViewModelTest`: com o banco vazio, inserir perfis pelo repositório (como faz a importação) → `startState` vai de `NEEDS_ONBOARDING` para `READY`, e `activeProfile` é o primeiro perfil.

**Validação**
- [ ] Portão mínimo verde. Também o portão de release, porque o fluxo de importação passa a ter um segundo ponto de entrada.
- [ ] Manual: `adb shell pm clear br.com.gabrielmorais.autocare` → abrir → "Restaurar de um arquivo" → escolher um backup exportado → prévia → importar → o Início aparece com os dados, as fotos e **nenhum perfil extra**. Repetir cancelando na prévia → continua no onboarding.

**Commit:** `Adiciona restaurar de um arquivo no primeiro acesso`

---

## S4 — Recursos sem uso

**Problema.** O `lintDebug` lista 21 `UnusedResources`. Isso pesa no APK e confunde quem mexe no app: por exemplo, existem dois conjuntos de ícone e o manifesto usa só `ic_autocare_logo`.

Lista atual, do relatório de lint (gerá-lo de novo antes de apagar, porque S1 a S3 podem mudar o resultado):
- Ícones e imagens: `mipmap/ic_launcher`, `mipmap/ic_launcher_round`, `mipmap/ic_autocare_logo_round`, `drawable/ic_launcher_background`, `drawable/ic_launcher_foreground`, `drawable/car_repair_placeholder`, `drawable/error`.
- Strings: `text_maintenance`, `text_unknow`, `text_description`, `text_my_maintenance`, `mileage_placeholder`, `notification_title`, `notification_description`, `channel_name`, `channel_description`, `notify_again`, `content_desc_open_menu`, `text_mileage_done`, `text_mileage_now`, `text_mileage_forecast`.

**Mudanças**
- [ ] Rodar `./gradlew lintDebug` e partir da lista nova.
- [ ] Antes de apagar, conferir cada string: a que tem um texto fixo equivalente no código, como o nome do canal em `NotificationUtils`, deve **passar a ser usada** em vez de apagada. A tradução fica centralizada e o texto fixo sai do código.
- [ ] Apagar o resto, incluindo as versões por densidade (`mipmap-*dpi/`, `mipmap-anydpi-v26/`, `mipmap-anydpi-v33/`).
- [ ] Em `lint { }` no `app/build.gradle`, tornar `UnusedResources` fatal (`error 'UnusedResources'`) para a lista não voltar a crescer.

**Testes**
- Nenhum teste novo. A suíte atual e o lint fatal são a rede de segurança.

**Validação**
- [ ] Portão mínimo e portão de release verdes. O release é o que confirma que o R8 e o `shrinkResources` não dependiam desses recursos.
- [ ] Instalar o APK de release e conferir o ícone no launcher, na tela de apps e na notificação de lembrete.

**Commit:** `Remove recursos sem uso e torna o UnusedResources fatal no lint`

---

## Depois das quatro

- [ ] Esvaziar a seção "Sobras conhecidas" de `plano-publicacao.md` e atualizar o "Andamento", que ainda descreve a etapa 9 como "sem push nem PR", embora ela já esteja em `main` pelo PR #9.
- [ ] Seguir para a etapa 10 (AdMob + UMP).
