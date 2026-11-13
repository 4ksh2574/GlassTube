# GlassTube Web

A responsive browser companion for the GlassTube Android app.

## Run locally

Open `index.html` from this folder in a modern browser. The deployed site receives its YouTube Data API v3 key from the repository's existing `YT_API_KEY` GitHub Actions secret.

## API key note

The web deployment reuses the same `YT_API_KEY` secret as the Android build. The deployment workflow writes it into a generated `config.js`; the key is not committed to the repository. **A key used by a browser is visible to visitors**, so this is a temporary convenience for this personal project, not a secure way to publish a public website. Keep the key restricted in Google Cloud and revisit the setup before making the site public or sharing it widely.

YouTube playback uses the official YouTube privacy-enhanced embed domain.

Saved videos are stored locally in this browser and are separate from the Android app's local library.

## Deployment

The `Deploy GlassTube Web` GitHub Actions workflow publishes this folder to GitHub Pages at:

https://4ksh2574.github.io/GlassTube/
