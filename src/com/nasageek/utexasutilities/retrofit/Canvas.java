package com.nasageek.utexasutilities.retrofit;

import java.util.List;

import com.nasageek.utexasutilities.model.canvas.CanvasCourse;

import retrofit.http.GET;

public interface Canvas {

    @GET("/api/v1/courses?include[]=term&access_token=12~9LxMuYQfyNAM4qI3e9mdzxq7AkRTfwpgnX9D1ge5DUx3oaNcapoMwmDvibDXNkH9")
    CanvasCourse.List courseList();
}
