const { connectToDb, getDb } = require("./db.js");

let db;
function connectToDatabase(cb) {
  let connected = false;
  connectToDb(function(error) {
    if (!error) {
      db = getDb();
      connected = true;
      cb(connected);
    } else {
      cb(connected);
      throw new Error("Failed to connect to the mongoDB database");
    }
  });
}

function getDatabase(){
  return db;
}

module.exports = { connectToDatabase, getDatabase };
