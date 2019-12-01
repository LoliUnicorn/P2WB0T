const inquirer = require("inquirer");
const fs = require("fs");

let baseConfig = fs.readFileSync("./baseConfig.txt", "utf8");
let config;
try {
  config = require("./config.json");
} catch (err) {
  config = null;
}

let prompts = [
  {
    type: "password",
    name: "encrypt",
    message: "Wpisz tu token właściciela."
  },
  {
    type: "input",
    name: "resttokens",
    message: "Wpisz tu inne tokeny.",
    validate: async function(answer, {token}) {
      let arr = answer.split(",");
      if (arr.length === 0) return "Nie wygląda mi to na prawidłową listę tokenów..";
      for (let entry of arr) if (entry.trim().length !== entry.length) return "Usuń proszę spację na początku/przed przecinkiem/po przecinku/na końcu.";
      return true;
    }
  },
  {
    type: "input",
    name: "port",
    message: "Wpisz port."
  },
  {
    type: "input",
    name: "ip",
    message: "Wpisz IP serwera."
  }
];

(async function() {
  console.log("Instalacja API");
  if (config) return console.log("Konfiguracja istnieje! Aby przygotować API od nowa, usuń plik konfiguracyjny.");

  const answers = await inquirer.prompt(prompts);

  baseConfig = baseConfig
    .replace("{{ownerToken}}", `"${answers.encrypt}"`)
    .replace("{{resttokens}}", `"${answers.resttokens.split(",").join("\", \"")}"`)
    .replace("{{port}}", `${answers.port}`)
    .replace("{{ip}}", `"${answers.ip}"`);

  fs.writeFileSync("./config.json", baseConfig);

  console.log("Konfiguracja została zapisana!");
}());
