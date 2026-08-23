#!/usr/bin/env node
/**
 * Limpa as URLs de foto que apontam para o Firebase Storage.
 *
 * O tier gratuito de Storage foi encerrado e o app migrou para o Cloudinary, mas
 * as URLs gravadas antes da migracao continuam no banco. Elas nunca mais vao
 * resolver: cada render dispara uma requisicao condenada a falhar e o app cai no
 * placeholder. Anular os campos remove esse trabalho inutil.
 *
 * Roda em modo simulacao por padrao. Use --apply para gravar de verdade.
 *
 *   npm install
 *   export GOOGLE_APPLICATION_CREDENTIALS=/caminho/da/service-account.json
 *   export FIREBASE_DATABASE_URL=https://<project-id>-default-rtdb.firebaseio.com
 *   node clear-legacy-photos.js            # simulacao
 *   node clear-legacy-photos.js --apply    # grava
 *
 * A chave de service account sai de: Firebase Console > Configuracoes do projeto >
 * Contas de servico > Gerar nova chave privada. Ela da acesso administrativo total
 * ao projeto - nao versione esse arquivo.
 */

const admin = require('firebase-admin');

const DEAD_HOST = 'firebasestorage.googleapis.com';
const apply = process.argv.includes('--apply');

const databaseURL = process.env.FIREBASE_DATABASE_URL;
if (!databaseURL) {
  console.error('Defina FIREBASE_DATABASE_URL antes de rodar.');
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.applicationDefault(),
  databaseURL,
});

const db = admin.database();

function isDead(value) {
  return typeof value === 'string' && value.includes(DEAD_HOST);
}

async function collectUpdates() {
  const updates = {};

  const users = await db.ref('Usuarios').once('value');
  users.forEach((user) => {
    if (isDead(user.child('photo').val())) {
      updates[`Usuarios/${user.key}/photo`] = null;
    }
  });

  const vehiclesByUser = await db.ref('vehicles').once('value');
  vehiclesByUser.forEach((userNode) => {
    userNode.forEach((vehicle) => {
      if (isDead(vehicle.child('photo').val())) {
        updates[`vehicles/${userNode.key}/${vehicle.key}/photo`] = null;
      }
    });
  });

  return updates;
}

async function main() {
  const updates = await collectUpdates();
  const paths = Object.keys(updates);

  if (paths.length === 0) {
    console.log('Nenhuma URL do Firebase Storage encontrada. Nada a fazer.');
    return;
  }

  console.log(`${paths.length} campo(s) apontando para o Storage antigo:`);
  paths.forEach((path) => console.log(`  ${path}`));

  if (!apply) {
    console.log('\nSimulacao: nada foi gravado. Rode de novo com --apply para limpar.');
    return;
  }

  // Update multi-path: atomico, e valor null apaga a chave.
  await db.ref().update(updates);
  console.log(`\n${paths.length} campo(s) limpos.`);
}

main()
  .catch((error) => {
    console.error('Falhou:', error.message);
    process.exitCode = 1;
  })
  .finally(() => db.goOffline());
