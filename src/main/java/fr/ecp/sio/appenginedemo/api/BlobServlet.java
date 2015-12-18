package fr.ecp.sio.appenginedemo.api;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.images.*;
import fr.ecp.sio.appenginedemo.model.User;
import fr.ecp.sio.appenginedemo.data.UsersRepository;

/**
 * Created by bensoussan on 13/12/2015.
 */
public class BlobServlet extends JsonServlet {


    // POST
    // Method called when a User post a file on the URL return by the GET method (using a redirection )
    // Key of uploadFile is saved in User Repository.
    // Return the User
    @Override
    protected User doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {

        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        User user = getAuthenticatedUser(req);
        if (user == null){
            throw new ApiException(400, "No authentification", "Need to be authentificated");
        }
        // Récupère une Map de tous les champs d'upload de fichiers
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);

        // Récupère la liste des fichiers uploadés dans le champ "uploadedFile"
        // (il peut y en avoir plusieurs si c'est un champ d'upload multiple, d'où la List)
        List<BlobKey> blobKeys = blobs.get("avatarFile");

        //"uploadedFile"
        if (blobKeys == null){
            throw new ApiException(400, "invalidKey", "Invalid KEY on formdata");
        }

        // Récupère la clé identifiant du fichier uploadé dans le Blobstore (à sauvegarder)
        String cleFichierUploade = blobKeys.get(0).getKeyString();

        String urlImage = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKeys.get(0)));

        user.avatar = urlImage;
        user.avatarKey = cleFichierUploade;
        // On enregistre la clé de l'avatar avec le User correspondant
        UsersRepository.saveUser(user);

        return user;
    }

    //  Method to open a UploadUrl in the BlobStore
    //  RETURN a User with the Field Avatar is loaded with this URL
    //  Using this Url to Post an image on BlobStore with the Key "avatarFile"
    //  and a User Authentificated
    @Override
    protected User doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {

        User user = getAuthenticatedUser(req);

        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        String Url  = blobstoreService.createUploadUrl("/upload");
        user.avatar = Url;

        return user;

    }
}
