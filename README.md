appengine-skeleton
=============================
-  La Technologie utilisée pour le stockage de l'avatar : BlobStore et ImageService
   Le service est gérer par la BlobServlet (Fonctionnement en 2 temps :
   Récupération d'une URL d'Upload (GET)
   Upload fait par le Client sur cette URL automatiquement redirigée sur
   le POST une fois le stockage enregistré dans le blobstore.
   La clé de l'image et l'url sont enregistrées par l'utilisateur.

-  Un utilisateur ne peut voir que les messages postés par un utilisateur qu'il suit

-  Users Repository
   Le stockage des informations de follow dans le Repository n'est pas implémenté.
   Les méthodes concernées retournent donc la liste complète de User
   et une methode de test de suivi entre 2 utilisateurs retourne toujours vrai.
   UsersServlet est en charge des appels de services concernant les relations.
   En revanche la création d'une nouvelle relation se fait dans UserServlet (followed = true)

-  The Users list can be seen by all but with hide private info
-  The whole Message list can be seen for instance by all in order to debug ( with hide private info)
-  The correct way is that a User can see only Messages of User followed. ( messages?author=2 )
