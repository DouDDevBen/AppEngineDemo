appengine-skeleton
=============================
-  La Technologie utilisée pour le stockage de l'avatar : BlobStore et ImageService
   Le service est gérer par la BlobServlet (Fonctionnement en 2 temps :
   Récupération d'une URL d'Upload (GET)
   Upload fait par le Client sur cette URL automatiquement redirigée sur
   le POST une fois le stockage enregistré dans le blobstore.
   La clé de l'image et l'url sont enregistrées par l'utilisateur.
   Pour modifié un avatar uploadé , il faut refaire ces 2 étapes.

-  Un utilisateur ne peut voir que les messages postés par un utilisateur qu'il suit

-  Users Repository
   Le stockage des informations de follow dans le Repository n'est pas implémenté.
   Les méthodes concernées retournent donc la liste complète de User
   et une methode de test de suivi entre 2 utilisateurs retourne toujours vrai.
   UsersServlet est en charge des appels de services concernant les relations.
   En revanche la création d'une nouvelle relation se fait dans UserServlet (followed = true)

-  Pour la gestion des relations d'un user ( users/1234/followers ):
   les paramètres limit et continuation Token ne sont pas mis dans le corps de la requête mais
   bien en paramêtre de celle-ci au même titre que followedBy et followerOf après redirection d'url.

-  La liste de User peut être vu par tout le monde mais les informations privées sont cachées.

-  La liste de message et vu pour le moment par tout le monde pour des soucis de debug. (/messages)
   Cette liste de messages complète ne sera pas visible à terme.
   Les infos privées des Users sont cachées. La bonne utilisation est d'utiliser le paramètre author
   (messages?author=2) pour voir ici tous les messages du User 2 si celui-ci est suivis par le User authentifié
   Comme j'ai fait l'hypothèse que tout le monde suit tous le monde pour le moment,
   on ne voit pas la différence. (vu que les relations du Repository ne sont pas encore implémentés.)





