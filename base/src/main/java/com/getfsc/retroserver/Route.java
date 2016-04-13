package com.getfsc.retroserver;

import com.getfsc.retroserver.request.RequestCaller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 下午2:27
 */
public class Route {
    public static final Route NotFound=new Route();
    private  String verb;
    private  String url;
    private RequestCaller caller;
    private String baseUrl;
    private BodyType bodyType;
    private List<String> headers=new ArrayList<>();
    public Route() {
    }

    public void addHeader(String header) {
        headers.add(header);
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCaller(RequestCaller caller) {
        this.caller = caller;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String toString() {
        return "Route{" +
                "verb='" + verb + '\'' +
                ", url='" + url + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                '}';
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public String getVerb() {
        return verb;
    }

    public String getUrl() {
        return url;
    }

    public RequestCaller getCaller() {
        return caller;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
