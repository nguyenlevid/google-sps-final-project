package com.google.sps.servlets;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for creating new tasks. */
@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    //get word from user
    JsonObject data = new Gson().fromJson(request.getReader(), JsonObject.class);
    String searchWord = data.get("input").getAsString();
    long timestamp = System.currentTimeMillis();

    //store word in the dataStore
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind("Word");
    FullEntity taskEntity =
        Entity.newBuilder(keyFactory.newKey())
            .set("content", searchWord)
            .set("timestamp", timestamp)
            .build();
    datastore.put(taskEntity);

  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //get the stored words
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    Query<Entity> query =
        Query.newEntityQueryBuilder().setKind("Word").setOrderBy(OrderBy.desc("timestamp")).build();
    QueryResults<Entity> results = datastore.run(query);

    //store the words in an array
    ArrayList<String> wordsList = new ArrayList<>();
    while (results.hasNext()) {
      Entity entity = results.next();
      String word = entity.getString("content");
      wordsList.add(word);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(wordsList));
  }
}
