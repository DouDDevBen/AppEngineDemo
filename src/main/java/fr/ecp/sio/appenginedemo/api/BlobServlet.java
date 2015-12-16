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

/**
 * Created by bensoussan on 13/12/2015.
 */
public class BlobServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        // Récupère une Map de tous les champs d'upload de fichiers
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);

        // Récupère la liste des fichiers uploadés dans le champ "uploadedFile"
        // (il peut y en avoir plusieurs si c'est un champ d'upload multiple, d'où la List)
        List<BlobKey> blobKeys = blobs.get("uploadedFile");

        // Récupère la clé identifiant du fichier uploadé dans le Blobstore (à sauvegarder)
        String cleFichierUploade = blobKeys.get(0).getKeyString();

        String urlImage = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKeys.get(0)));

        String autreChamp = req.getParameter("idUser");
        // On enregistre la clé de l'avatar avec le User correspondant




    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
