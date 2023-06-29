class MainPage {
    init() {
        const loginForm = document.getElementById('loginForm');

        loginForm.onsubmit = event => {
            event.preventDefault();
            const form = event.target;
            const data = new FormData(form);

            const value = Object.fromEntries(data.entries());

            httpPost(
                form.action,
                JSON.stringify(value),
                (response) => {
                    window.location = 'secured/index.html'
                },
                (response) => {
                    alert(response.message);
                }
            );

            return false;
        }

        const resetPinForm = document.getElementById('resetPinForm');
        resetPinForm.onsubmit = event => {
            event.preventDefault();
            const form = event.target;
            const data = new FormData(form);

            const value = Object.fromEntries(data.entries());

            if (value.newPin !== value.newPinConfirm) {
                alert('Password & configuration are not the same')
                return;
            }

            httpPost(
                form.action,
                JSON.stringify(value),
                (res) => {
                    alert('Password updated');
                    resetPinForm.style.display = 'none';
                },
                (response) => {
                    alert(response.message);
                }
            );

            return false;
        }

        const resetPin = document.getElementById("reset-pin");
        resetPin.onclick = (e) => {
            if (resetPinForm.style.display === 'block') {
                resetPinForm.style.display = 'none';
            } else {
                resetPinForm.style.display = 'block';
                resetPinForm.style.left = e.target.offsetLeft + 'px';
                resetPinForm.style.top = (e.target.offsetTop + e.target.offsetHeight) + 'px';
            }
        }
    }
}

window.addEventListener("load", e => {
    let page = new MainPage();
    page.init();
    window.currentPage = page;
});