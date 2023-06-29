httpGet(
    '/api/secured/dashboard/',
    (res) => {
        document.getElementById('productUuid').innerHTML = res.productUuid;

        let networks = document.getElementById('networks');
        networks.innerHTML = '';

        for(let net of res.networkDetails) {
            appendChildTag(networks, 'li', `<strong>${net.displayName}</strong><br/>${net.name} / ${net.address} / ${net.mask}`);
        }
    }
);