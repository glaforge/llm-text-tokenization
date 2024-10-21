async function tokenize() {
    const prompt = document.getElementById("prompt").value;
    const model = document.getElementById("model").value;

    const tokensDiv = document.getElementById("tokens");
    const countSpan = document.getElementById("count");
    countSpan.textContent = '';

    if (prompt.trim().length === 0) return;

    const submitBtn = document.getElementById("submitBtn");
    submitBtn.loading = true;
    submitBtn.disabled = true;
    tokensDiv.replaceChildren();

    let path = "/tokens";
    if (model === "gemini") {
        path += "/gemini";
    }

    const response = await fetch(path, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        },
        mode: "cors",
        body: JSON.stringify({
            prompt, model
        })
    });
    if (response.ok) {
        var json = {tokensInfo: {tokenIds: [], tokens: []}};
        try {
            json = await response.json();
        } catch (e) {
            console.log("An error occurred", e);
            alert("An error occurred: " + e);
        }

        const tokensInfo = json.tokensInfo[0];

        // display the tokens
        for (var i = 0; i < tokensInfo.tokenIds.length; i++) {
            var tokenId = tokensInfo.tokenIds[i];
            var tokenString = decodeURIComponent(escape(window.atob(tokensInfo.tokens[i]))); // handles UTF-8 better

            const span = document.createElement('span');
            span.style.backgroundColor = 'hsl(' + toHslAngle(tokenString) + ', 100%, 90%)';
            span.textContent = tokenString;
            span.title = tokenId;
            tokensDiv.appendChild(span);
        }

        // display the token count
        if (tokensInfo.tokenIds.length > 0) {
            countSpan.textContent = tokensInfo.tokenIds.length;
        }

    } else {
        console.log("An error occurred", response.statusText);
        alert("An error occurred: " + response.statusText);
    }

    submitBtn.loading = false;
    submitBtn.disabled = false;
}

function toHslAngle(s) {
    return [...s].reduce((acc, c) => Math.imul(31, acc) + c.charCodeAt(0) | 0, 0) % 360;
}

function clearAll() {
    document.getElementById("prompt").value = "";
    document.getElementById("count").textContent = "";
    document.getElementById("tokens").replaceChildren();
}