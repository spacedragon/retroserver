package example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/10
 * Time: 上午11:37
 */

public interface TestService {
    @GET("users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);

}
