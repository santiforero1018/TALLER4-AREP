notFound = (() => {
    function returnToMain(){
        let baseUrl = window.location.href.replace(/\/[^\/]*$/, '/');
        // Redirigir a client.html utilizando la URL base actual y una ruta relativa
        window.location.href = baseUrl + "client.html";
    }

    return {
        returnToMain,
    }
})();