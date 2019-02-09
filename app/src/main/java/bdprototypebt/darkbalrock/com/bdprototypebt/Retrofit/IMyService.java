package bdprototypebt.darkbalrock.com.bdprototypebt.Retrofit;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IMyService{
    @POST("saveDevices")
    @FormUrlEncoded
    Observable<String> saveDevices(@Field("Name") String Name,
                                     @Field("Address") String Address);
}
