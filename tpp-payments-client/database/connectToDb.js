const { connectToDb, getDb } = require("./db.js");

let db;
function connectToDatabase(cb) {
  connectToDb(function(error) {
    if (!error) {
      db = getDb();
      cb(true);
    } else {
      cb(false);
      throw new Error("Failed to connect to the mongoDB database");
    }
  });
}

function getDatabase(){
  return db;
}

module.exports = { connectToDatabase, getDatabase };
