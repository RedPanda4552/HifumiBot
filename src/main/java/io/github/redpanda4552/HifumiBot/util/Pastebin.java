// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.util;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Pastebin {

  public static String sendPaste(String title, String paste) throws IOException {
    RequestBody body =
        new FormBody.Builder()
            .add("api_paste_private", "1")
            .add("api_option", "paste")
            .add("api_user_key", "")
            .add("api_paste_name", title)
            .add("api_paste_expire_date", "1H")
            .add("api_dev_key", HifumiBot.getSelf().getConfig().integrations.pastebinApiKey)
            .add("api_paste_code", paste)
            .build();
    Request req =
        new Request.Builder().url("https://pastebin.com/api/api_post.php").post(body).build();
    Response res = HifumiBot.getSelf().getHttpClient().newCall(req).execute();
    return res.body().string();
  }
}
