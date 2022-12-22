package com.sensorflow.backendcom.newrelic;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.sensorflow.backendcom.newrelic.model.Trace;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TraceReporter {

    private static final OkHttpClient httpCLient =   new OkHttpClient.Builder()
            .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                    .addHeader("Api-Key", "NRII-cWPAwp_yze4GDhoQkvhF5JQQ3_wZ-Z5S")
                    .addHeader("Data-Format", "newrelic")
                    .addHeader("Data-Format-Version", "1")
                    .addHeader("Content-Type", "application/json")
                    .build()))
            .addInterceptor(new GzipRequestInterceptor())
            .callTimeout(5, TimeUnit.SECONDS)
            .build();

    private static void noticeError(Trace trace) {
        String json = trace.toJson();
        RequestBody body = RequestBody.create(json,
                MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://trace-api.eu.newrelic.com/trace/v1")
                .post(body)
                .build();

        Call call = httpCLient.newCall(request);
        try {
            Response response = call.execute();
            response.body().close();
        } catch (IOException e) {
//            ignore it's just monitoring info
        }
    }
}
