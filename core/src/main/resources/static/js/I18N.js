const i18nLabels = {
    'en': {
        'things': 'Components',
        'chooseTask': 'Choose Task',
        'empty': 'Empty'
    },
    'ru': {
        'things': 'Устройства',
        'chooseTask': 'Выберите задачу',
        'empty': 'Пусто'
    }
}

class I18N {
    defaultLanguage = 'en'
    language;

    constructor() {
        let lang = getCookie("Language");
        if (lang === undefined) {
            lang = this.defaultLanguage;
        }
        this.language = lang;

        let i18nNodes = document.querySelectorAll("*[i18n]");
        for(let node of i18nNodes) {
            let key = node.getAttribute("i18n");
            node.innerHTML = this.val(key);
        }
    }

    val(key) {
        if (key == null) {
            return '';
        }
        try {
            if (key.startsWith('i18n.val')) {// case when title contains i18n call
                return eval(key);
            } else if (key.indexOf('~') > -1) {// case when title consists from class name & title
                let parts = key.split('~');
                return this.val0(parts[0] + '.title', parts[1]);
            }
        } catch (e) {
            console.log(e);
        }

        let set = i18nLabels[this.language];
        if (!set) {
            set = i18nLabels[this.defaultLanguage];
        }
        const val = set[key];

        return val === undefined ? key : val;
    }

    valArgs(key, args) {
        if (args === undefined || args === null || args.length === 0) {
            return this.val(key);
        }
        if (args.length === 1) {
            return this.val0(key, args[0]);
        }
        if (args.length === 2) {
            return this.val1(key, args[0], args[1]);
        }
        if (args.length === 3) {
            return this.val2(key, args[0], args[1], args[2]);
        }
    }

    val0(key, param0) {
        return this.val(key).replaceAll('{0}', param0);
    }

    val1(key, param0, param1) {
        return this.val0(key, param0).replaceAll('{1}', param1);
    }

    val2(key, param0, param1, param2) {
        return this.val1(key, param0, param1).replaceAll('{2}', param2);
    }

    titlesMapExpiration = 0;
    titlesMap = new Map();

    titlesRequests = new Map();
    titlesRequested = false;

    _refreshTitlesMap() {
        if (this.titlesRequested) {
            return;
        }
        this.titlesRequested = true;
        setTimeout(() => {
            this.titlesRequested = false;
        }, 5000);
        const cacheKey = `/api/secured/environment/available`;
        httpGet('/api/secured/environment/available', availableEnvs => {
            window.db.cacheFile(cacheKey, JSON.stringify(availableEnvs));
            this._processAvailableEnvsResponse(availableEnvs);
        }, (reason) => {
            window.db.cacheFileGet(cacheKey, (cachedData) => {
                let availableEnvs = JSON.parse(cachedData);
                this._processAvailableEnvsResponse(availableEnvs);
            }, () => {
                throw '' + reason;
            });
        });
    }

    _processAvailableEnvsResponse(availableEnvs) {
        this.titlesMapExpiration = Date.now() + 60000;
        let titlesMap = new Map();
        availableEnvs.forEach(environment => {
            titlesMap.set(environment.uuid, environment.title);
        });
        this.titlesMap = titlesMap;
        let titlesRequests = this.titlesRequests;
        this.titlesRequests = new Map();
        titlesRequests.forEach((key, callback) => {
            let title = this.titlesMap.get(key);
            if (title === undefined) {
                title = key;
            }
            callback(title);
        });
    }

    environmentTitle(environmentId, callback) {//interface method uses in dashboard
        if (Date.now() > this.titlesMapExpiration) {
            this.titlesMap.clear();
        }
        let title = this.titlesMap.get(environmentId);
        if (title === undefined) {
            this.titlesRequests.set(callback, environmentId);
            this._refreshTitlesMap()
        } else {
            callback(title);
        }
    }
}