// file index.jsp

<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>

<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

<!DOCTYPE html>

<html>
    <head>
        <title>Upload Test</title>
        <meta charset="utf-8" />
    </head>
    <body>
        <form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
            <p>
                <input type="text" name="idUser">
            </p>
            <p>
            <label>Fichier à envoyer : <input type="file" name="uploadedFile" /></label>
            </p>
            <p>
            <label>Description du fichier : <input type="text" name="description" /></label>
            </p>
            <p>
            <input type="submit" />
            </p>

        </form>
    </body>
</html>