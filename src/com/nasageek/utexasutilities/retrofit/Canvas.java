
package com.nasageek.utexasutilities.retrofit;

import com.nasageek.utexasutilities.model.canvas.ActivityStreamItem;
import com.nasageek.utexasutilities.model.canvas.Assignment;
import com.nasageek.utexasutilities.model.canvas.CanvasCourse;
import com.nasageek.utexasutilities.model.canvas.File;
import com.nasageek.utexasutilities.model.canvas.Folder;
import com.nasageek.utexasutilities.model.canvas.Module;
import com.nasageek.utexasutilities.model.canvas.OAuthResponse;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

public interface Canvas {

    //@formatter:off

    // hardcoded access token for chris
    // 12~9LxMuYQfyNAM4qI3e9mdzxq7AkRTfwpgnX9D1ge5DUx3oaNcapoMwmDvibDXNkH9
    @GET("/api/v1/courses?include[]=term&state[]=available&state[]=completed")
    CanvasCourse.List courseList(
        @Header("Authorization") String canvas_auth_token);

    @GET("/api/v1/courses/{course_id}/assignments?include[]=submission")
    Assignment.List assignmentsForCourse(
        @Header("Authorization") String canvas_auth_token, 
        @Path("course_id") String course_id);

    @GET("/api/v1/courses/{course_id}/files")
    File.List filesForCourse(
        @Header("Authorization") String canvas_auth_token, 
        @Path("course_id") String course_id);

    @GET("/api/v1/folder/{folder_id}/files")
    File.List filesForFolder(
        @Header("Authorization") String canvas_auth_token, 
        @Path("folder_id") String folder_id);

    @GET("/api/v1/folder/{folder_id}/folders")
    Folder.List foldersForFolder(
        @Header("Authorization") String canvas_auth_token, 
        @Path("folder_id") String folder_id);
    
    @GET("/api/v1/courses/{course_id}/modules?include[]=items")
    Module.List modulesForCourse(
        @Header("Authorization") String canvas_auth_token,
        @Path("course_id") String course_id);
    
    @GET("/api/v1/courses/{course_id}/activity_stream")
    ActivityStreamItem.List activityStreamForCourse(
        @Header("Authorization") String canvas_auth_token, 
        @Path("course_id") String course_id);

    @FormUrlEncoded
    @POST("/login/oauth2/token")
    OAuthResponse postAuthCode(
        @Field("client_id") String client_id,
        @Field("client_secret") String client_secret,
        @Field("redirect_uri") String redirect_uri,
        @Field("code")  String code);

    @DELETE("/login/oauth2/token")
    void deauthorize(
        @Header("Authorization") String canvas_auth_token, Callback<Object> cb);
}
