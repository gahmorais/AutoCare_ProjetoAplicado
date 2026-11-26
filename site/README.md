# Site do AutoCare

Arquivos estáticos para o domínio próprio cadastrado como site do desenvolvedor na Play e no AdMob.
Nada aqui é usado pelo build do app.

| Arquivo | Para quê |
| --- | --- |
| `index.html` | Página inicial: o que é o app, link para a Play e para a política. |
| `privacidade.html` | Política de privacidade (pt-BR). É a URL do Play Console e a que o app abre em Perfil → Sobre. |
| `app-ads.txt` | Autoriza o AdMob a vender anúncios do app. Sem ele, a receita é limitada. |
| `estilo.css` | Estilo das duas páginas, com tema claro e escuro. |

## Antes de publicar

1. Troque os placeholders em todos os arquivos:
   - `[NOME DA ORGANIZAÇÃO]`: o mesmo nome verificado no Play Console;
   - `[E-MAIL DE CONTATO]`: o e-mail público de contato do desenvolvedor;
   - `[DATA DE PUBLICAÇÃO]`: o dia em que a política entra no ar;
   - `[LINK DA PLAY STORE]`: `https://play.google.com/store/apps/details?id=br.com.gabrielmorais.autocare`, depois de publicado.
2. Em `app-ads.txt`, troque `pub-0000000000000000` pelo ID de editor do AdMob
   (AdMob → Configurações → Informações da conta → ID do editor).
3. Em `app/build.gradle`, troque `privacyPolicyPlaceholder` pela URL real de `privacidade.html`.
   Enquanto ela for o placeholder, a release assinada falha de propósito.

## Onde publicar

O `app-ads.txt` precisa estar na **raiz** do domínio: `https://<seu-domínio>/app-ads.txt`.
Uma subpasta ou uma página de projeto do GitHub Pages (`usuario.github.io/AutoCare/`) não serve.
Qualquer hospedagem de arquivos estáticos funciona: GitHub Pages com domínio personalizado,
Cloudflare Pages, Netlify ou o servidor do próprio domínio.

Depois de publicar, confira:

```sh
curl -sI https://<seu-domínio>/app-ads.txt     # 200 e content-type text/plain
curl -sI https://<seu-domínio>/privacidade.html
```

O mesmo domínio vai no campo **Site** da ficha do app no Play Console. O AdMob lê o `app-ads.txt` a partir
desse campo, por isso a verificação só acontece depois que o app estiver publicado e vinculado no AdMob.

## Quando o app mudar

A política descreve o que o app faz hoje. Se uma versão nova passar a coletar algo, pedir uma permissão nova
ou trocar o SDK de anúncios, atualize `privacidade.html` e a seção Segurança dos dados no Play Console
na mesma versão.
