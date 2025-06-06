<script>
    import {onMount} from 'svelte';

    let token = '';

    let playlist = '';

    async function handleGo() {
        console.log("fetching playlist...");

        const tokenResponse = await fetch('/api/token');
        token = await tokenResponse.text();
        console.log("Token recieved:", token);

        const myHeaders = new Headers({
            Authorization: 'Bearer ' + token
        });

        const response = await fetch("https://api.spotify.com/v1/playlists/" + playlist.split("/").pop(), {
            headers: myHeaders,
            method: 'GET'
        });

        const data = await response.json();
        console.log(data);
    }
</script>

<style>
    :root{
        min-height: 100vh;
        --headers: plum;
        --text: plum; 
        --names: plum;
        --links: thistle;
        --font-family: "Century Gothic", cursive;
        --curve: 10px;
        --spot-black: #121212;
        --spot-green: #1ED760;
        --borders: 2px solid var(--spot-green);
        background: linear-gradient(to bottom, var(--spot-black), var(--spot-green));
    }
    h1{
        color: white;
        font-family: var(--font-family);
        font-size:30px !important;
        font-weight: 1;
        border: var(--borders);
        background-color: var(--spot-black);
        padding:10px;
        text-align: center;
        border-radius: var(--curve);
        width: 50%;
        margin: auto;
    }
    h2{
        color: white;
        font-family: var(--font-family);
        font-size:20px !important;
        font-weight: 1;
        padding:10px;
        text-align: center;
        margin: 2px auto;
    }
    input{
        color: white;
        font-family: var(--font-family);
        font-size:20px !important;
        font-weight: 1;
        background-color: var(--spot-black);
        padding:10px;
        text-align: center;
        margin: 2px auto;
        border-color: var(--spot-green);
        width: 450px;
        border-radius: var(--curve);
    }
    .wrapper {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 10px;
        margin: 2px auto;
    }   
    button{
        color: white;
        font-family: var(--font-family);
        font-size:20px !important;
        font-weight: 1;
        background-color: var(--spot-black);
        padding:5px;
        text-align: center;
        margin: 2px auto;
        transition: all 0.2s;
        border-color: var(--spot-green);
        border-radius: var(--curve);
    }
    button:hover{
        color: var(--spot-black);
        background-color: var(--spot-green);
    }
</style>

<head>
 <title>Spotify Spreadsheet App</title>
</head>

<h1>Spotify Spreadsheet Doohickey</h1>

<h2>Please paste the link to your Spotify playlist here</h2>
<div class="wrapper">
    <input type="text" placeholder="https://open.spotify.com/playlist/xxxxxxxx" id="playlist-link" bind:value={playlist}/>
</div>
<div class="wrapper">
    <button id="load-playlist" on:click={handleGo}>Go</button>
</div>

