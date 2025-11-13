package api.test;

import api.endpoints.UserEndpoints;
import api.payload.User;
import api.utilities.DataProviders;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DDTests {

    @Test(priority = 1, dataProvider = "AllData", dataProviderClass = DataProviders.class)
    public void testPostUser(String userID, String userName, String fName, String lName, String email, String pwd, String ph) {

        String username = (userName == null) ? "" : userName.trim();

        User userPayload = new User();
        userPayload.setId(Integer.parseInt(userID.trim()));
        userPayload.setUsername(username);
        userPayload.setFirstName(fName);
        userPayload.setLastName(lName);
        userPayload.setEmail(email);
        userPayload.setPassword(pwd);
        userPayload.setPhone(ph);

        Response res = UserEndpoints.createUser(userPayload);
        res.then().log().all();
        Assert.assertEquals(res.getStatusCode(), 200);
    }

    @Test(priority = 2, dependsOnMethods = "testPostUser", dataProvider = "UserNames", dataProviderClass = DataProviders.class)
    public void testDeleteUserByName(String userName) {
        // fail-fast if provider returned null/empty
        Assert.assertNotNull(userName, "DataProvider returned null username");
        userName = userName.trim();
        Assert.assertFalse(userName.isEmpty(), "DataProvider returned empty username");

        // Wait for the user created earlier (if needed)
        Assert.assertTrue(waitForUserAvailable(userName, 8), "User not available after creation: " + userName);

        Response res = UserEndpoints.deleteUser(userName);
        res.then().log().all();
        Assert.assertEquals(res.getStatusCode(), 200);
    }

    private boolean waitForUserAvailable(String username, int timeoutSec) {
        if (username == null || username.trim().isEmpty()) return false;
        username = username.trim();

        int wait = 0;
        while (wait < timeoutSec * 1000) {
            Response r = UserEndpoints.readUser(username);
            if (r.getStatusCode() == 200) return true;
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
            wait += 500;
        }
        return false;
    }
}
