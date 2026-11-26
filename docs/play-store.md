# Play Store: ficha e formulários do AutoCare

Textos e respostas prontos para colar no Play Console na primeira publicação (etapa 11 de
[plano-publicacao.md](plano-publicacao.md)). Se o app mudar, revise as seções correspondentes antes
de enviar a versão.

## Ficha da loja

**Nome do app** (até 30 caracteres): `AutoCare: manutenção do carro`

**Descrição curta** (até 80 caracteres):

> Lembretes de manutenção do carro, sem cadastro. Seus dados ficam no aparelho.

**Descrição completa** (até 4.000 caracteres):

> Nunca mais perca a troca de óleo, o alinhamento ou a revisão do carro.
>
> O AutoCare guarda o histórico de manutenção dos seus veículos e avisa antes de cada serviço vencer,
> pela quilometragem e pela data.
>
> SEM CADASTRO, SEM SENHA
> Abra e comece a usar. Seus veículos, manutenções e fotos ficam salvos só no seu aparelho.
>
> A FAMÍLIA TODA NUM APP SÓ
> Crie um perfil para cada pessoa e veja, na tela inicial, tudo o que está para vencer em todos os carros
> da casa. Precisa passar um carro para outra pessoa? Transfira o veículo de perfil com o histórico junto.
>
> PREVISÃO DA PRÓXIMA TROCA
> Escolha o serviço, informe a quilometragem e o AutoCare calcula quando fazer a próxima, a partir da
> distância que você roda por mês. Uma régua mostra quanto já foi percorrido e quanto falta.
>
> LEMBRETES
> Receba uma notificação dias antes da data prevista. Os lembretes voltam sozinhos depois de reiniciar o
> celular.
>
> SEUS DADOS, SUA CÓPIA
> Com o backup do Android ativado, os dados voltam quando você troca de celular. Você também pode
> exportar tudo num arquivo e importar em outro aparelho.
>
> O AutoCare é gratuito e exibe anúncios.

**Categoria:** Auto e veículos. **Tags:** manutenção, carro, lembretes.

**Contato:** `[E-MAIL DE CONTATO]` e o site do domínio próprio (ver [site/README.md](../site/README.md)).
**Política de privacidade:** `https://<seu-domínio>/privacidade.html`.

### Recursos gráficos

| Recurso | Formato | Origem |
| --- | --- | --- |
| Ícone | 512 × 512 PNG, 32 bits | `app/src/main/ic_autocare_logo-playstore.png` |
| Gráfico de recursos | 1024 × 500 PNG ou JPEG, sem transparência | **a criar** |
| Capturas de celular | 2 a 8, PNG ou JPEG, 1080 × 2400 | `docs/play-store/capturas/` |

As capturas foram feitas no emulador (API 36), com o build `./gradlew installDebug -PsemAnuncios=true`
para não aparecer anúncio de teste, e com dois perfis para mostrar o recurso de família.

## Segurança dos dados

| Pergunta | Resposta |
| --- | --- |
| O app coleta ou compartilha algum dos tipos de dados obrigatórios? | **Sim** (pelo SDK do AdMob) |
| Todos os dados coletados são criptografados em trânsito? | **Sim** |
| Os usuários podem pedir a exclusão dos dados? | **Não há solicitação**: os dados do app ficam no aparelho e são apagados ao desinstalar. Para os dados de anúncios, o controle é do Google (ID de publicidade). |

Dados do próprio app (perfis, veículos, manutenções, fotos): **não coletados**, porque não saem do
aparelho. O Auto Backup vai para a conta Google do usuário e não conta como coleta pelo desenvolvedor.

Dados coletados pelo SDK do Google Mobile Ads (confira com a página oficial
[Divulgação de dados do SDK dos anúncios para dispositivos móveis do Google](https://developers.google.com/admob/android/privacy/play-data-disclosure)
antes de enviar, porque a lista muda com a versão do SDK; a do app é a 25.5.0):

| Tipo de dado | Coletado | Compartilhado | Finalidade | Opcional? |
| --- | --- | --- | --- | --- |
| Localização aproximada | Sim | Sim | Publicidade, análise, prevenção de fraude | Não |
| IDs do dispositivo ou outros IDs | Sim | Sim | Publicidade, análise, prevenção de fraude | Não |
| Interações com o app | Sim | Sim | Publicidade, análise, prevenção de fraude | Não |
| Registros de falhas e diagnóstico | Sim | Sim | Análise, prevenção de fraude | Não |
| Outros dados de desempenho do app | Sim | Sim | Análise, prevenção de fraude | Não |

## Conteúdo do app

| Formulário | Resposta |
| --- | --- |
| Anúncios | **Sim**, o app contém anúncios. |
| ID de publicidade | **Sim**, usado para publicidade (o SDK declara `com.google.android.gms.permission.AD_ID`). |
| Acesso ao app | Todas as funcionalidades estão disponíveis sem restrições (não há login). |
| Público-alvo | **18 anos ou mais**, para ficar fora da política Famílias. O app não atrai crianças. |
| Classificação de conteúdo | Categoria "Utilitário, produtividade, comunicação ou outro". Sem violência, sexo, linguagem imprópria, drogas, jogos de azar, compras ou interação entre usuários. Respostas "Não" em todo o questionário; o resultado esperado é Livre. |
| App de notícias / saúde / finanças / governo | Não. |
| Permissões sensíveis | Nenhuma permissão restrita: câmera e notificações são de tempo de execução comuns; não há alarme exato com `USE_EXACT_ALARM`. |

## Ordem da primeira publicação

1. Conta de organização verificada (D-U-N-S e site).
2. Secrets do AdMob cadastrados; tag `v1.0.0` → `release.yml` gera AAB, APK e `mapping.txt`.
3. Play Console → criar o app → **Play App Signing** ativado (a chave do CI vira a chave de upload).
4. Teste interno: enviar o AAB **e o `mapping.txt`** (Pacote de app → Explorador → Fazer upload do arquivo de desofuscação).
5. Preencher a ficha, a segurança dos dados e o conteúdo do app com as respostas acima.
6. Instalar pela Play num aparelho real e passar pela validação da etapa 11.
7. Promover para produção.
8. AdMob: vincular o app publicado e conferir a verificação do `app-ads.txt`.
