package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        breed = breed.toLowerCase(Locale.ROOT).trim();
        String url = "https://dog.ceo/api/breed/" + breed + "/list";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new BreedNotFoundException("Empty response from dog.ceo API");
            }

            String body = response.body().string();
            JSONObject json = new JSONObject(body);
            String status = json.optString("status");

            if (!response.isSuccessful() || !"success".equalsIgnoreCase(status)) {
                throw new BreedNotFoundException("Breed not found: " + breed);
            }

            JSONArray subBreedsArray = json.getJSONArray("message");
            List<String> subBreeds = new ArrayList<>();

            for (int i = 0; i < subBreedsArray.length(); i++) {
                subBreeds.add(subBreedsArray.getString(i));
            }

            Collections.sort(subBreeds);
            return subBreeds;

        } catch (IOException e) {
            throw new BreedNotFoundException("Failed to fetch sub-breeds for: " + breed);
        }
        catch (Exception e) {
            throw new BreedNotFoundException("Unexpected error when parsing response for: " + breed);
        }
    }
}