package api.test;

import api.endpoints.UserEndpoints;
import api.endpoints.UserEndpoints_2;
import api.payload.User;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserTests {

    Faker fake;
    User userPayload;

    @BeforeClass    //Use BeforeMethod
    public void setupData(){

        fake = new Faker();
        userPayload = new User();

        userPayload.setId(fake.idNumber().hashCode());
        userPayload.setUsername(fake.name().username());
        userPayload.setFirstName(fake.name().firstName());
        userPayload.setLastName(fake.name().lastName());
        userPayload.setEmail(fake.internet().emailAddress());
        userPayload.setPassword(fake.internet().password());
        userPayload.setPhone(fake.phoneNumber().cellPhone());
    }

    @Test(priority = 1)
    public void testPostUser() {
        Response res = UserEndpoints.createUser(userPayload);
        res.then().log().all();
        Assert.assertEquals(res.getStatusCode(), 200);
    }

    @Test(priority = 2, dependsOnMethods = {"testPostUser"})
    public void testGetUserByName(){
        Response res = UserEndpoints.readUser(userPayload.getUsername());
        res.then().log().all();
        Assert.assertEquals(res.getStatusCode(),200,"User is not created.");
    }

    @Test(priority = 3, dependsOnMethods = {"testPostUser"})
    public void testUpdateUserByName(){
        userPayload.setFirstName(fake.name().firstName());
        userPayload.setLastName(fake.name().lastName());
        userPayload.setEmail(fake.internet().emailAddress());

        Response res = UserEndpoints.updateUser(userPayload.getUsername(), userPayload);
        res.then().log().body();
        Assert.assertEquals(res.getStatusCode(),200);

        Response resAfterUpdate = UserEndpoints.readUser(userPayload.getUsername());
        Assert.assertEquals(resAfterUpdate.getStatusCode(),200);
    }

    @Test(priority = 4, dependsOnMethods = {"testUpdateUserByName"})
    public void testDeleteUserByName(){
        Response res = UserEndpoints.deleteUser(userPayload.getUsername());
        Assert.assertEquals(res.getStatusCode(),200);
    }
}
