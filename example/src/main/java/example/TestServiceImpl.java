package example;

import com.getfsc.retroserver.RetroController;
import com.getfsc.retroserver.annotation.ContentType;
import com.getfsc.retroserver.annotation.Controller;
import com.getfsc.retroserver.auth.Check;
import com.getfsc.retroserver.jwt.JWT;
import com.getfsc.retroserver.server.ServerOptions;
import retrofit2.Call;
import retrofit2.http.Path;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/10
 * Time: 下午12:01
 */
@Controller("/")
public class TestServiceImpl extends RetroController implements TestService {


    public TestServiceImpl() {
    }

    @Inject
    ServerOptions options;

    @JWT
    @Check("admin")
    @ContentType(ContentType.JSON)
    public Call<List<Repo>> listRepos(@Path("user") String user) {
        List<Repo> result = Arrays.asList(new Repo(user));

        return ok(result);
    }


}
