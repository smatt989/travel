package com.example.app.Routes

import com.example.app.SlickRoutes
import com.example.app.models.Activity

trait AppRoutes extends SlickRoutes{


  get("/") {
    <html>
      <body>
        <div id="app"></div>
        <script src="/front-end/dist/bundle.js"></script>
      </body>
    </html>
  }


  get("/activities") {
    contentType = formats("json")

    Activity.getAll
  }
}
