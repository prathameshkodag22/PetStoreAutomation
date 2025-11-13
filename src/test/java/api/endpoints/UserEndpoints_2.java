package api.endpoints;

import api.payload.User;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.ResourceBundle;

import static io.restassured.RestAssured.*;

// Created to perform Create,Read,Update and Delete requests to the user API.
public class UserEndpoints_2 {

    //method created for getting URL's from properties file
    static ResourceBundle getURL(){
        ResourceBundle routes = ResourceBundle.getBundle("Routes");
        return routes;
    }

    public static Response createUser(User payload){

        String posturl = getURL().getString("post_url");
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(payload)
                        .when()
                        .post(posturl);
        return response;
    }

    public static Response readUser(String userName){

        String geturl = getURL().getString("get_url");
        Response response =
                given()
                        .pathParam("username",userName)
                        .when()
                        .get(geturl);
        return response;
    }

    public static Response updateUser(String userName, User payload){

        String updateurl = getURL().getString("update_url");
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .pathParam("username",userName)
                        .body(payload)
                        .when()
                        .put(updateurl);
        return response;
    }

    public static Response deleteUser(String userName){

        String deleteurl = getURL().getString("delete_url");
        Response response =
                given()
                        .pathParam("username",userName)
                        .when()
                        .delete(deleteurl);
        return response;
    }
}
