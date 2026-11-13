# GlassTube Web

A responsive browser companion for the GlassTube Android app.

## Run locally

Open `index.html` from this folder in a modern browser. Add a YouTube Data API v3 key from the settings button to enable discovery and search.

## API key safety

The web app stores the API key in this browser's localStorage. It is not committed to GitHub or embedded by the deployment workflow. Browser keys are visible to the browser, so create a **separate web key**, restrict it to YouTube Data API v3, and set an HTTP referrer restriction for:

- `https://4ksh2574.github.io/GlassTube/*`

Do not reuse an unrestricted key. YouTube playback uses the official YouTube privacy-enhanced embed domain.

Saved videos are stored locally in this browser and are separate from the Android app's local library.

## Deployment

The `Deploy GlassTube Web` GitHub Actions workflow publishes this folder to GitHub Pages at:

https://4ksh2574.github.io/GlassTube/
