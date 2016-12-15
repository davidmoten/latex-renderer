package com.github.davidmoten.latex;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.function.Supplier;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.github.davidmoten.aws.helper.StandardRequestBodyPassThrough;
import com.github.davidmoten.util.RandomString;

public class RendererHandler {

    public String render(Map<String, Object> input, Context context) {

        LambdaLogger log = context.getLogger();

        log.log("input=" + input);

        // expects full request body passthrough from api gateway integration
        // request
        StandardRequestBodyPassThrough request = StandardRequestBodyPassThrough.from(input);
        String latex = request.queryStringParameter("latex")
                .orElseThrow(exception("'latex' parameter not found"));

        String stage = request.stage().orElseThrow(exception("stage value not found"));
        String bucket = "latex-renderer-" + stage;
        AmazonS3Client s3 = new AmazonS3Client();
        String s3Id = nextId();
        byte[] bytes = Renderer.renderPng(latex);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        s3.putObject(bucket, s3Id, new ByteArrayInputStream(bytes), metadata);
        String url = s3.getResourceUrl(bucket, s3Id);
        // must throw an exception to from java lambda to get 302
        // redirection to work! The error message (the url) is mapped by
        // the integration response part of the API Gateway to a 302
        // status code with Location header equal to the url value
        throw new RuntimeException(url);
    }

    private Supplier<? extends RuntimeException> exception(String message) {
        return () -> new RuntimeException(message);
    }

    private static String nextId() {
        return RandomString.next(8);
    }

}
