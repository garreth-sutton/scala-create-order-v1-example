object Main extends App {
  
  val key =    "PASTE API KEY HERE"
  val secret = "PASTE API SECRET HERE"

  import play.api.libs.json._
  import java.util.Base64
  import java.nio.charset.StandardCharsets
  import javax.crypto.Mac
  import javax.crypto.spec.SecretKeySpec
  import scalaj.http._
  import java.util.concurrent.atomic.AtomicLong

  // Creating order via v1 API to match customers usage
  val urlPath = "/v1/order/new";
  val url = "https://api.bitfinex.com/v1/order/new";  

  // Wrapping date nonce in an AtomicLong obj to copy the customer's usage
  val nonce = new AtomicLong(System.currentTimeMillis()).toString

  // Using the same request body as the customer
  val body: JsValue = Json.obj(
    "request" -> urlPath,
    "nonce" -> nonce,
    "symbol" -> "xrpusd",
    "amount" -> "28.0", // I've raised the amount value to meet the 'minimum amount' requirement and make the order valid
    "price" -> "0.05",
    "side" -> "buy",
    "type" -> "exchange trailing-stop",
    "exchange" -> "bitfinex",
    "is_hidden" -> false,
    "is_postonly" -> false,
    "use_all_available" -> 0,
    "ocoorder" -> false,
    "buy_price_oco" -> "",
    "sell_price_oco" -> 0.05
  )

  // Generate payload
  val payload = Base64.getEncoder.encodeToString(body.toString().getBytes(StandardCharsets.UTF_8))

  // Generate signature
  val mac: Mac = Mac.getInstance("HmacSHA384")
  val secretKey: SecretKeySpec = new SecretKeySpec(secret.getBytes, "HmacSHA384")
  mac.init(secretKey)
  val hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
  val sig =  hmac.map("%02x".format(_)).mkString

  // Transmit to v1 create order endpoint (POST)
  val response = Http(url).postData(body.toString())
    .header("X-BFX-SIGNATURE", sig)
    .header("X-BFX-APIKEY", key)
    .header("X-BFX-PAYLOAD", payload)
    .header("content-type", "application/json")
    .option(HttpOptions.readTimeout(10000)).asString

  // Parse the response as JSON and print to console
  val jsonResponse = Json.parse(response.body)

  System.out.println(jsonResponse);
}