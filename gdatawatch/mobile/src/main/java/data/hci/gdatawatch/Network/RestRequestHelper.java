package data.hci.gdatawatch.Network;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;


/**
 * Created by user on 2016-04-01.
 */
public class RestRequestHelper {
    private static String url = "http://203.246.112.184:8081/KServer/";
    private RestRequest restRequest;
    private RestAdapter retrofit;
    private static RestRequestHelper instance;

    public static RestRequestHelper newInstance(){
        if(instance == null)
            instance = new RestRequestHelper();
        return instance;
    }

    public RestRequestHelper(){
        retrofit = new RestAdapter.Builder()
                .setEndpoint(url)
                .build();

        //Service Setup
        restRequest = retrofit.create(RestRequest.class);
    }
    public interface RestRequest{

        @FormUrlEncoded
        @POST("/EnrollUser")
        void enrollUser(
                @Field("name") String name,
                @Field("birth") String birth,
                @Field("gender") int gender,
                @Field("job") String job,
                @Field("level") int level,
                Callback<Integer> euCallback
        );

        @FormUrlEncoded
        @POST("/Environment")
        void enviroData(
                @Field("data") String data,
                Callback<Integer> evCallback
        );

        @FormUrlEncoded
        @POST("/situation")
        void sitData(
            @Field("data") String data,
            Callback<Integer>sitCallback
        );

        @FormUrlEncoded
        @POST("/location")
        void getPlace(
                @Field("lat") double lat,
                @Field("lng") double lng,
                Callback<String> placeCallback
        );
    }

    public void enrollUser(String name, String birth, int gender, String job, int level, Callback<Integer> euCallback){
        restRequest.enrollUser(name, birth, gender, job, level, euCallback);
    }

    public void enviroData( String data, Callback<Integer> evCallback){
        restRequest.enviroData(data, evCallback);
    }

    public void sitData(String data, Callback<Integer> sitCallback){
        restRequest.sitData(data, sitCallback);
    }

    public void getPlace(double lat, double lng, Callback<String> placeCallback){
        restRequest.getPlace(lat, lng, placeCallback);
    }
}
