Array.prototype.contains = function(obj) { //eslint-disable-line no-extend-native
  let i = this.length;
  while (i--) {
    if (this[i] === obj) {
      return true;
    }
  }
  return false;
};
const fsn = require("fs-nextra");
const restify = require("restify");
const colorThief = new (require("color-thief"))();
const config = require("../config.json");
const server = restify.createServer({name: "API", url: config.ip});
server.use(restify.plugins.queryParser());
const snek = require("snekfetch");
const { Canvas } = require("canvas-constructor");
const bfs = require("buffer-image-size");
const Jimp = require("jimp");


const cache = new Map();

function authorize(req, res, next) {
  if (req.headers.authorization === config.tokens.owner) {
    req.params.tokenType = "owner";
    next();
  } else {
    if (config.tokens.rest.contains(req.headers.authorization)) {
      req.params.tokenType = "access";
      next();
    } else res.send(403, {success: false, error: {code: 403, body: "Forbidden"}});
  }
}

server.on("NotFound", function(req, res) {
  return res.send(404, {success: false, error: {code: 404, body: "Not Found"}});
});

server.get("/api", function(req, res) {
  res.send(200, {success: true, response: null});
});

/**
 * @api {get} api/checkToken Sprawdza token
 * @apiName checkToken
 * @apiDescription Używane do sprawdzenia prawidłowości tokenów
 * @apiGroup Token
 * @apiVersion 1.0.1
 * @apiHeader {string} authorization token
 * @apiSuccess {Boolean} success Czy zapytanie się udało
 * @apiSuccess {Object} response Informacja o tokenie
 * @apiSuccess {String} response.type Typ tokenu
 * @apiSuccess {Boolean} response.valid Prawidłowość tokenu
 * @apiError {Boolean} success Czy zapytanie się udało
 * @apiError {Object} response Odpowiedź
 * @apiError {Boolean} response.valid Prawidłowość tokenu
 * @apiSuccessExample {json}
 *     HTTP/1.1 200 OK
 *     {
 *       "success": true,
 *       "response": {
 *         "valid": true,
 *         "type": "owner"
 *       }
 *     }
 * @apiErrorExample {json}
 *     HTTP/1.1 200 OK
 *     {
 *       "success": true,
 *       "response": {
 *         "valid": false
 *       }
 *     }
 */
server.get("/api/token/checkToken", function(req, res) {
  if (req.headers.authorization === config.tokens.owner) {
    req.params.tokenType = "owner";
    return res.send({success: true, response: {type: req.params.tokenType, valid: true}});
  } else {
    if (config.tokens.rest.contains(req.headers.authorization)) {
      req.params.tokenType = "accesss";
      return res.send({success: true, response: {type: req.params.tokenType, valid: true}});
    } else return res.send({success: true, response: {valid: false}});
  }
});

/**
 * @api {get} api/image/blurple Blurple
 * @apiName Blurple
 * @apiHeader {string} authorization token
 * @apiGroup Generator
 * @apiDescription Koloryzuje podane zdjęcia na blurple
 * @apiVersion 1.0.6
 * @apiParam {String} avatarURL Link do awataru
 * @apiParam {Boolean} [reverse=false] Odwrócenie kolorów: zmienia schemat kolorów z biało-blurple na blurple-biało (bez classic (patrz niżej) usuwa kolor biały, czyli "dark mode")
 * @apiParam {Boolean} [classic=false] Tryb klasyczny: stary algorytm, nie używa dark blurple (#4E5D94)
 * @apiSuccess {Boolean} success Czy zapytanie się udało
 * @apiSuccess {object} image Objekt ze zdjęciem
 * @apiSuccess {String} image.type Typ, "Buffer"
 * @apiSuccess {Array} image.data Buffer Array, czyli zdjęcie
 * @apiError {Object} error Błąd
 * @apiError {Number} error.code Kod błędu
 * @apiError {String} error.body Treść błędu
 * @apiError {String} error.description Opis błędu, co poszło nie tak
 * @apiSuccessExample {json}
 *     HTTP/1.1 200 OK
 *     {
 *       "success": true,
 *       "image": {
 *         "type": "Buffer",
 *         "data": [...]
 *       }
 *     }
 */
server.get("/api/image/blurple", authorize, async function(req, res) {
  if (!req.query.avatarURL) return res.send(400, {success: false, error: {code: 400, body: "Bad Request", description: "Brak linka do awataru"}});
  if (cache.has("blurple" + req.query.avatarURL.replace(/\.gif/g, ".png"))) {
    return res.send({success: true, image: cache.get("blurple" + req.query.avatarURL.replace(/\.gif/g, ".png"))});
  }
  let body;
  try {
    body = await snek.get(req.query.avatarURL.replace(/\.gif/g, ".png")).then(r => r.body);
  } catch (err) {
    return res.send(400, {success: false, error: {code: 400, body: "Bad Request", description: "Nie udało się pobrać awataru"}});
  }
  let reverse = req.query.reverse === "true";
  let classic = req.query.classic === "true";
  try {
    let img;
    if (classic) img = await Jimp.read(body).then(j => j.colorType(0).contrast(1).getBufferAsync(Jimp.MIME_PNG).then(b => Jimp.read(b).then((j) => j.contrast(1).colorType(6).getBufferAsync(Jimp.MIME_PNG))));
    else img = await Jimp.read(body).then(j => j.colorType(0).contrast(1).getBufferAsync(Jimp.MIME_PNG).then(b => Jimp.read(b).then((j) => j.contrast(0.7).colorType(6).getBufferAsync(Jimp.MIME_PNG))));
    const canvas = new Canvas(bfs(img).width, bfs(img).height);
    canvas.addImage(img, 0, 0, bfs(img).width, bfs(img).height);
    const imData = canvas.getImageData();
    for (let i = 0; i < imData.data.length; i += 4) {
      let r = imData.data[i];
      let g = imData.data[i + 1];
      let b = imData.data[i + 2];
      if (!reverse) {
        if (classic) {
          if (r === 255 && g === 255 && b === 255) continue;
          r = 114;
          g = 137;
          b = 218;
        } else {
          if (r === 255 && g === 255 && b === 255) continue;
          if (255 - r >= 5 && 255 - g >= 5 && 255 - b >= 5) { r = 78; g = 93; b = 148; } else { r = 114; g = 137; b = 218; }
        }
      } else {
        if (classic) {
          if (!(r === 255 && g === 255 && b === 255)) {
            r = 255;
            g = 255;
            b = 255;
          } else {
            r = 114;
            g = 137;
            b = 218;
          }
        } else {
          if (255 - r >= 5 && 255 - g >= 5 && 255 - b >= 5) { r = 78; g = 93; b = 148; } else { r = 114; g = 137; b = 218; }
        }
      }
      imData.data[i] = r;
      imData.data[i + 1] = g;
      imData.data[i + 2] = b;
    }
    canvas.putImageData(imData, 0, 0);
    const buff = await canvas.toBufferAsync();
    cache.set("blurple" + req.query.avatarURL.replace(/\.gif/g, ".png"), buff);
    return res.send({success: true, image: buff});
  } catch (err) {
    return res.send(400, {success: false, error: {code: 400, body: "Bad Request", description: "Nie udało się pobrać awataru"}});
  }
});

/**
 * @api {get} api/image/primColor Primary Color
 * @apiName Primary Color
 * @apiHeader {string} authorization token
 * @apiGroup Generator
 * @apiDescription Przewodzący kolor
 * @apiParam {String} imageURL Link do zdjęcia
 * @apiVersion 1.0.5
 * @apiSuccess {Boolean} success Czy zapytanie się udało
 * @apiSuccess {Number[]} color Kolor w RGB
 * @apiError {Object} error Błąd
 * @apiError {Number} error.code Kod błędu
 * @apiError {String} error.body Treść błędu
 * @apiError {String} eror.description Opis błędu, co poszło nie tak
 * @apiSuccessExample {json}
 *     HTTP/1.1 200 OK
 *     {
 *       "success": true,
 *       "color": [114, 137, 218]
 *     }
 * @apiErrorExample {json}
 *     HTTP/1.1 400 Bad Request
 *     {
 *       "success": false,
 *       "error": {
 *         "code": 400,
 *         "body": "Bad Request",
 *         "description": "Brak linka do zdjęcia"
 *       }
 *     }
 */
server.get("/api/image/primColor", authorize, async function(req, res) {
  if (!req.query.imageURL) return res.send(400, {success: false, error: {code: 400, body: "Bad Request", description: "Brak linka do zdjęcia"}});
  if (cache.has("primColor" + req.query.imageURL)) {
    return res.send({success: true, color: cache.get("primColor" + req.query.imageURL)});
  }
  let body;
  try {
    body = await snek.get(req.query.imageURL.replace(/\.gif/g, ".png")).then(r => r.body);
  } catch (err) {
    return res.send(400, {success: false, error: {code: 400, body: "Bad Request", description: "Nie udało się pobrać zdjęcia"}});
  }
  const arr = colorThief.getColor(body);
  cache.set("primColor" + req.query.imageURL, arr);
  return res.send({success: true, color: arr});
});

/**
 * @api {get} api/polacz Połącz
 * @apiName połącz
 * @apiDescription Łączy x zdjęć zestawiając je obok siebie.
 * @apiGroup Grafika
 * @apiHeader {string} authorization token
 * @apiParam {string[]} zdjecie[] Wszystkie zdjęcia do połączenia
 * @apiVersion 1.0.5
 * @apiSuccess {Boolean} success Czy zapytanie się udało
 * @apiSuccess {Object} image Zdjecie
 * @apiSuccess {String} image.type Typ, "Buffer"
 * @apiSuccess {Array} image.data Samo zdjęcie
 * @apiError {Boolean} success Czy zapytanie się udało
 * @apiError {Object} error Błąd
 * @apiError {Number} error.code Numer błędu
 * @apiError {String} error.body Treść błędu
 * @apiError {String} error.description Opis błędu, czyli co poszło nie tak.
 * @apiSuccessExample {json}
 *     HTTP/1.1 200 OK
 *     {
 *       "success": true,
 *       "image": {
 *          "type": "Buffer",
 *          "data": [...]
 *        }
 *     }
 * @apiErrorExample {json}
 *     HTTP/1.1 500 Internal Server Error
 *     {
 *       "success": false,
 *       "error": {
 *         "code": 500,
 *         "body": "Internal Server Error",
 *         "description": "Nie udało się stworzyć zdjęcia"
 *       }
 *     }
 */
server.get("/api/polacz", authorize, async function(req, res) {
  if (!req.query.zdjecie || req.query.zdjecie.length === 0) return res.send(400, {success: false, error: 400, body: "Bad Request", description: "Nie podano żadnych zdjęć"});
  const zdjecia = [];
  const dims = { width: 0, height: 0 };
  function a(body) {
    const { width, height } = bfs(body);
    zdjecia.push({image: body, width, height});
  }
  for (const url of req.query.zdjecie) {
    let body;
    try {
      body = await snek.get(url).then((r) => r.body);
    } catch (err) {
      return res.send(400, {success: false, error: 400, body: "Bad Request", description: "Nie udało się pobrać zdjęcia " + url});
    }
    a(body);
  }
  dims.height = zdjecia.reduce((p, c) => Math.max(p, c.height), 0);
  dims.width = zdjecia.reduce((p, c) => p + c.width * (dims.height / c.height), 0);
  const canva = new Canvas(dims.width, dims.height);
  let offset = 0;
  for (const zdjecie of zdjecia) {
    const dimsE = {width: 0, height: 0};
    const powiekszenie = dims.height / zdjecie.height;
    dimsE.width = zdjecie.width * powiekszenie;
    dimsE.height = zdjecie.height * powiekszenie;
    canva.addImage(zdjecie.image, offset, 0, dimsE.width, dimsE.height);
    offset += dimsE.width;
  }
  res.send({success: true, image: canva.toBuffer()});
});

setInterval(() => {
  cache.clear();
}, 300e3);

