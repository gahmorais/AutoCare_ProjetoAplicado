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
   firebase deploy --only database
   ```

   [`database.rules.json`](./database.rules.json) restringe `Usuarios/{uid}` e `vehicles/{uid}`
   ao próprio usuário autenticado e deixa `lista-servicos` como somente leitura.

   > ⚠️ Conceder `".read": "auth != null"` **no nó pai** (`Usuarios`, `vehicles`) expõe os dados
   > de todos os usuários: as regras cascateiam para baixo e não podem ser revogadas no filho.
   > Como o cadastro é self-service, qualquer pessoa cria uma conta e lê o banco inteiro via
   > REST. A permissão precisa ficar no nível `$uid`, como no arquivo versionado.

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
./gradlew testDebugUnitTest      # 36 testes JVM
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
> adb shell am instrument -w \
>   br.com.gabrielmorais.autocare.test/androidx.test.runner.AndroidJUnitRunner
> ```

O build requer **JDK 17** (AGP 8.0.2).

