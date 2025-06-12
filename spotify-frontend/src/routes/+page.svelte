<script>
    import {onMount} from 'svelte';

    let token = '';

    let playlist = '';

    let loading;

    let loaded = true;

    $: tracks = [];

    let progress = 0;
    let totalRequests = 0;

    async function handleGo() {
        tracks = [];
        loading = true;
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

        let data = await response.json();
        console.log(data);

        tracks.push.apply(tracks, data.tracks.items);

        let numTracks = data.tracks.total;

        totalRequests = Math.ceil(numTracks/100 - 1);

        for (progress = 0; progress < totalRequests; progress++) {
            const trackResponse = await fetch(progress === 0 ? data.tracks.next : data.next, {
                headers: myHeaders,
                method: 'GET'
            });

            data = await trackResponse.json();
            console.log(data);
            tracks.push.apply(tracks, data.items);
        }

        console.log(tracks);
        loaded = false;
        loading = false;
    }

    function sendToBackend() {
        console.log("Sending data to backend...");

        fetch ('/api/playlist', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({tracks})
        })

        .then(response => response.blob())
        .then(blob => {
            var file = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = file;
            a.download = 'playlist.xlsx'
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(file);
        })
    }
</script>

<style>
     :root {
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
        background: var(--spot-black);
    }
    
    h1 {
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

    h2 {
        color: white;
        font-family: var(--font-family);
        font-size:20px !important;
        font-weight: 1;
        padding:10px;
        text-align: center;
        margin: 2px auto;
    }

    input {
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

    button {
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

    button:hover {
        color: var(--spot-black);
        background-color: var(--spot-green);
    }

    table {
        border-collapse: collapse;
        border: 2px solid var(--spot-green);
        font-family: sans-serif;
        font-size: 1rem;
        letter-spacing: 1px;
        margin: auto;
        color: white;
    }

    th,
    td {
        border: 1px solid var(--spot-green);
        padding: 8px 10px;
    }

    td:last-of-type {
        text-align: center;
    }

    .progressBarFiller {
        color: var(--spot-black);
        font-family: var(--font-family);
        font-size:20px;
        font-weight: 1;
        background-color: var(--spot-green);
        height: 30px;
        border-radius: var(--curve);
        margin: auto;
        position: relative;
    }

    .progressBar {
        background-color: lightgrey;
        font-weight: 1;
        height: 30px;
        width: 450px;
        border-radius: var(--curve);
        margin: auto;
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

<div class="wrapper">
    {#if !loaded}
    <button on:click={sendToBackend}>Send to Backend</button>
    {/if}
</div>

{#if loading}
    <div class="progressBar">
        {#if totalRequests = 0}
        	<div class="progressBarFiller" style="width: 450px; background-color: lightgrey">Processing...</div>
        {:else if totalRequests != 0}
            <div class="progressBarFiller" style="width: {(progress/totalRequests)*450}px; left:{-225+(((progress/totalRequests)*450)/2)}px">{progress}/{totalRequests}</div>
        {/if}
    </div>
{/if}

{#if !loaded}
    <table>
        <thead>
            <tr>
                <th>Track name</th>
                <th>Artist</th>
                <th>Album</th>
            </tr>
        </thead>
        <tbody>
            {#each tracks as track}
                {@const trackKey = track.track}
                <tr>
                    <td>{track.track.name}</td>
                    <td>{track.track.artists.map(artist => artist.name).join(', ')}</td>
                    <td>{track.track.album.name}</td>
                </tr>
            {/each}
        </tbody>
    </table>
{/if}