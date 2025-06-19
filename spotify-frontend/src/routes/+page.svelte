<script>
    import {onMount} from 'svelte';
    import {token} from '$lib/store';

    let playlist = '';

    function handleGo() {
        if (playlist) {
            const playlistId = playlist.split('/').pop();
            window.location.href = `/playlist/${playlistId}`;
        } else {
            alert('Please enter a valid Spotify playlist link.');
        }
    }

    onMount(async() => {
        const tokenResponse = await fetch('/api/token');
        $token = await tokenResponse.text();
        console.log("Token recieved:", $token);
    });
</script>

<style>
   @import '$lib/styles.css';    
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