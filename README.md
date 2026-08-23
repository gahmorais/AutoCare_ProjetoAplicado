## Aplicativo AutoCare
### Uma maneira simples de fazer o controle de manutenção do seu veículo

![image](./imgs/profile.png)
![image](./imgs/cars.png)
![image](./imgs/account.png)
![image](./imgs/car-details.png)
![image](./imgs/add_maintenance.png)

## Configuração

O projeto depende de um projeto Firebase (Authentication, Realtime Database e Storage).

1. Crie o projeto no [Firebase Console](https://console.firebase.google.com/) e registre o app
   com o applicationId `br.com.gabrielmorais.autocare`.
2. Baixe o `google-services.json` e coloque-o em `app/google-services.json`.
   **Esse arquivo não é versionado** — sem ele o build falha no plugin `com.google.gms.google-services`.
3. Habilite o provedor *E-mail/senha* em Authentication.
4. Publique as regras de segurança versionadas neste repositório:

   ```sh
   firebase deploy --only database,storage
   ```

   - [`database.rules.json`](./database.rules.json) — restringe `Usuarios/{uid}` e `vehicles/{uid}`
     ao próprio usuário autenticado e deixa `lista-servicos` como somente leitura.
   - [`storage.rules`](./storage.rules) — restringe `{uid}/**` ao próprio usuário, limita o upload
     a imagens de até 5 MB.

   > ⚠️ As regras padrão em modo de teste liberam leitura e escrita para qualquer um.
   > Publique as regras acima antes de expor o app a usuários reais.

5. Popule o nó `lista-servicos` do Realtime Database com os tipos de serviço:

   ```json
   {
     "lista-servicos": {
       "0": { "name": "Troca de óleo", "mileageChange": 10000, "mustBeDoneBefore": 12 }
     }
   }
   ```

O build requer **JDK 17** (AGP 8.0.2).

