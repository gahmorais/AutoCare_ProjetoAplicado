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

O build requer **JDK 17** (AGP 8.0.2).

