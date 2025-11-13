package api.test;

import api.endpoints.UserEndpoints_2;
import api.payload.User;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UserTests_2 {

    Faker fake;
    User userPayload;

    @BeforeClass
    public void setupData() {
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
        Response res = UserEndpoints_2.createUser(userPayload);
        res.then().log().all();
        Assert.assertEquals(res.getStatusCode(), 200, "User creation failed.");
    }

    @Test(priority = 2, dependsOnMethods = {"testPostUser"})
    public void testGetUserByName() {
        String username = userPayload.getUsername();
        Assert.assertNotNull(username, "Username is null!");
        Assert.assertFalse(username.trim().isEmpty(), "Username is empty!");

        // Step 1: Wait for user creation to propagate
        Assert.assertTrue(waitForUserAvailable(username, 15),
                "User not available after creation: " + username);

        // Step 2: Add extra retry logic for flaky environments
        Response res = null;
        int attempts = 0;
        int maxAttempts = 5;
        int delayMs = 2000;

        while (attempts < maxAttempts) {
            res = UserEndpoints_2.readUser(username);
            if (res.getStatusCode() == 200) {
                res.then().log().all();
                Assert.assertEquals(res.getStatusCode(), 200, "User retrieval failed.");
                return;
            }

            System.out.println("Attempt " + (attempts + 1) + ": User not yet found (Status: "
                    + res.getStatusCode() + "), retrying in " + delayMs + "ms...");
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Assert.fail("Test interrupted while waiting for user availability.");
            }
            attempts++;
        }

        // Step 3: Fail test after all retries
        Assert.fail("User not found after " + maxAttempts + " retries. Last status: " +
                (res != null ? res.getStatusCode() : "No Response"));
    }


    @Test(priority = 3, dependsOnMethods = {"testPostUser"})
    public void testUpdateUserByName() {
        userPayload.setFirstName(fake.name().firstName());
        userPayload.setLastName(fake.name().lastName());
        userPayload.setEmail(fake.internet().emailAddress());

        Response res = UserEndpoints_2.updateUser(userPayload.getUsername(), userPayload);
        res.then().log().body();
        Assert.assertEquals(res.getStatusCode(), 200, "User update failed.");

        Response resAfterUpdate = UserEndpoints_2.readUser(userPayload.getUsername());
        Assert.assertEquals(resAfterUpdate.getStatusCode(), 200, "User update verification failed.");
    }

    @Test(priority = 4, dependsOnMethods = {"testUpdateUserByName"})
    public void testDeleteUserByName() {
        Response res = UserEndpoints_2.deleteUser(userPayload.getUsername());
        Assert.assertEquals(res.getStatusCode(), 200, "User deletion failed.");
    }

    private boolean waitForUserAvailable(String username, int timeoutSec) {
        if (username == null || username.trim().isEmpty()) return false;
        username = username.trim();

        long endTime = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < endTime) {
            Response r = UserEndpoints_2.readUser(username);
            if (r.getStatusCode() == 200) return true;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}
