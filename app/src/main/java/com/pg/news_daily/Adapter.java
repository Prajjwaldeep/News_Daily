package com.pg.news_daily;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Activity context;
    private ArrayList<ModelClass> modelClassArrayList;

    public Adapter(Activity context, ArrayList<ModelClass> modelClassArrayList) {
        this.context = context;
        this.modelClassArrayList = modelClassArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelClass newsArticle = modelClassArrayList.get(position);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NewsWebViewActivity.class);
                intent.putExtra("url", newsArticle.getUrl());
                context.startActivity(intent);
            }
        });

        if (newsArticle.getPublishedAt() != null) {
            holder.mtime.setText("Published At: " + newsArticle.getPublishedAt());
        } else {
            holder.mtime.setText("");
        }

        if (newsArticle.getAuthor() != null) {
            holder.mauthor.setText(newsArticle.getAuthor());
        } else {
            holder.mauthor.setText("");
        }

        if (newsArticle.getTitle() != null) {
            holder.mheading.setText(newsArticle.getTitle());
        } else {
            holder.mheading.setText("");
        }

        if (newsArticle.getDescription() != null) {
            holder.mcontent.setText(newsArticle.getDescription());
        } else {
            holder.mcontent.setText("");
        }

        if (newsArticle.getUrlToImage() != null) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(newsArticle.getUrlToImage()).into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

//        holder.mtime.setText("Published At: " + newsArticle.getPublishedAt());
//        holder.mauthor.setText(newsArticle.getAuthor());
//        holder.mheading.setText(newsArticle.getTitle());
//        holder.mcontent.setText(newsArticle.getDescription());
//        Glide.with(context).load(newsArticle.getUrlToImage()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return modelClassArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mheading, mcontent, mauthor, mtime;
        CardView cardView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mheading = itemView.findViewById(R.id.mainheading);
            mcontent = itemView.findViewById(R.id.content);
            mauthor = itemView.findViewById(R.id.author);
            mtime = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.imageview);
            cardView = itemView.findViewById(R.id.cardview);
        }
    }

    // Helper method to fetch news data from NewsAPI
    public void fetchNewsData() {
        String apiKey = "1569c1a06cdf4b9f896f688099f2bf1f"; // Replace with your actual API key
        String url = "https://newsapi.org/v2/top-headlines?country=in&apiKey=" + apiKey;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle API request failure
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    Type type = new TypeToken<NewsApiResponse>() {}.getType();
                    NewsApiResponse apiResponse = gson.fromJson(responseData, type);

                    if (apiResponse != null && apiResponse.getArticles() != null) {
                        ArrayList<ModelClass> newsArticles = apiResponse.getArticles();

                        // Update the modelClassArrayList on the main thread
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                modelClassArrayList.clear();
                                modelClassArrayList.addAll(newsArticles);
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }

    // Helper class to parse the API response
    private static class NewsApiResponse {
        private ArrayList<ModelClass> articles;

        public ArrayList<ModelClass> getArticles() {
            return articles;
        }
    }
}
