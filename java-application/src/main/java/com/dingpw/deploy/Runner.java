package com.dingpw.deploy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author 丁朋伟@600100@18511694468
 */
public class Runner extends AbstractVerticle {

    private Vertx vertx = Vertx.vertx();

    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(req -> {
            MultiMap params = req.params();
            req.bodyHandler(totalBuffer -> {
                System.out.println("params = " + totalBuffer.toString());
                Map<String, Object> paramsMap = Json.decodeValue(totalBuffer, Map.class);
                if (params.contains("repository")) {
                    Map<String, Object> repository = (Map<String, Object>) paramsMap
                        .get("repository");
                    if ("hornbook-service".equals(repository.get("name"))) {
                        deployHornbook();
                    }
                }
            });
            req.response()
                .putHeader("content-type", "text/plain")
                .end("ok");
        }).listen();
    }

    public static void main(String[] args) {
        new Runner().deployHornbook();
    }

    private void deployHornbook() {
        String path = "/home/ubuntu/applications/hornbook/hornbook-service/";
        String executeGit = "git pull --rebase origin";
        String executeGradle = "gradle clean build autoDeploy -x test";

//        String path = "/Users/pwd/workspace/dingpw/github-hook-service/ignore/hornbook-service";
//        String executeGit = "pwd";
        String cmd = executeGit + " && " + executeGradle;
        execute(path,cmd);
    }

    private void execute(String path,String cmd) {
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(new String[]{"/bin/sh", "-c", cmd},null,new File(path));
            process.waitFor();
            InputStream in = process.getInputStream();
            BufferedReader bs = new BufferedReader(new InputStreamReader(in));
            List<String> list = new ArrayList<>();
            String result;
            while ((result = bs.readLine()) != null) {
                System.out.println("job result [" + result + "]");
                list.add(result);
            }
            in.close();
            bs.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
