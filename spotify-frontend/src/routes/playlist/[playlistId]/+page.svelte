<script>
    import {onMount} from 'svelte';
    import {token} from '$lib/store';
    import { page } from '$app/stores';

    $: playlistId = $page.params.playlistId;

    let loading;

    let loaded = false;

    $: tracks = [];

    let progress = 0;
    let totalRequests = 0;

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

    onMount(async() => {
        tracks = [];
        loading = true;
        console.log("fetching playlist...");

        console.log("Token: ", $token);

        const myHeaders = new Headers({
            Authorization: 'Bearer ' + $token
        });

        console.log("Headers constructed.");

        const response = await fetch("https://api.spotify.com/v1/playlists/" + playlistId, {
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
        loaded = true;
        loading = false;
    });

    function parseTime(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    }

    function parseSongLength(trackLength) {
        let seconds = Math.round(trackLength/1000);
        let minutes = Math.floor(seconds/60);
        seconds %= 60;
        let formattedSeconds = seconds < 10 ? '0' + seconds : seconds;
        return `${minutes}:${formattedSeconds}`; 
    }
</script>

<style>
    @import '$lib/styles.css';
</style>

<head>
    <title>Spotify Spreadsheet App: Table</title>
</head>

<h1>Spotify Spreadsheet Doohickey: Table</h1>

<div class="wrapper">
    {#if loaded}
    <button on:click={sendToBackend}>Send to Backend</button>
    {/if}
</div>

{#if loading}
    <div class="progressBar">
        {#if totalRequests = 0}
        	<div class="progressBarFiller" style="width: 450px; background-color: lightgrey">Processing... </div>
        {:else if totalRequests != 0}
            <div class="progressBarFiller" style="width: {(progress/totalRequests)*450}px; left:{-225+(((progress/totalRequests)*450)/2)}px"> {progress}/{totalRequests}</div>
        {/if}
    </div>
{/if}

{#if loaded}
    <table>
        <thead>
            <tr>
                <th>Track</th>
                <th>Artist</th>
                <th>Album</th>
                <th>Popularity</th>
                <th>Added At</th>
                <th>Duration</th>
            </tr>
        </thead>
        <tbody>
            {#each tracks as track}
                {@const trackKey = track.track}
                <tr>
                    <td>{track.track.name}</td>
                    <td>{track.track.artists.map(artist => artist.name).join(', ')}</td>
                    <td>{track.track.album.name}</td>
                    <td>{track.track.popularity}</td>
                    <td>{parseTime(track.added_at)}</td>
                    <td>{parseSongLength(track.track.duration_ms)}</td>
                </tr>
            {/each}
        </tbody>
    </table>
{/if}