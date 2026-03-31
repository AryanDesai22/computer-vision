#!/usr/bin/env python3
"""
download_models.py
------------------
Downloads pre-trained Torch (.t7) style transfer models into the models/ directory.
These are the fast neural style transfer models from Justin Johnson's repository.

Usage:
    python download_models.py
"""

import os, urllib.request, sys

MODELS = {
    "vangogh.t7":      "https://cs.stanford.edu/people/jcjohns/fast-neural-style/models/eccv16/the_wave.t7",
    "candy.t7":        "https://cs.stanford.edu/people/jcjohns/fast-neural-style/models/instance_norm/candy.t7",
    "mosaic.t7":       "https://cs.stanford.edu/people/jcjohns/fast-neural-style/models/instance_norm/mosaic.t7",
    "starry_night.t7": "https://cs.stanford.edu/people/jcjohns/fast-neural-style/models/instance_norm/starry_night.t7",
    "la_muse.t7":      "https://cs.stanford.edu/people/jcjohns/fast-neural-style/models/instance_norm/la_muse.t7",
    "udnie.t7":        "https://cs.stanford.edu/people/jcjohns/fast-neural-style/models/instance_norm/udnie.t7",
}

# Fallback alternate source (GitHub releases)
ALT_BASE = "https://github.com/jcjohnson/fast-neural-style/releases/download/v1.0/"

def download(name, url):
    os.makedirs("models", exist_ok=True)
    dest = os.path.join("models", name)
    if os.path.exists(dest):
        print(f"  [skip] {name} already exists")
        return
    print(f"  Downloading {name} ...", end=" ", flush=True)
    try:
        urllib.request.urlretrieve(url, dest)
        size = os.path.getsize(dest) / (1024 * 1024)
        print(f"done ({size:.1f} MB)")
    except Exception as e:
        print(f"FAILED: {e}")
        # try alternate
        alt_url = ALT_BASE + name
        try:
            print(f"  Retrying from alternate source...", end=" ", flush=True)
            urllib.request.urlretrieve(alt_url, dest)
            print("done")
        except Exception as e2:
            print(f"FAILED again: {e2}")
            print(f"  Please manually download from:\n    {url}\n  and place as: {dest}")

if __name__ == "__main__":
    print("=== Downloading style transfer models ===")
    for name, url in MODELS.items():
        download(name, url)
    print("\nAll models ready in models/ directory.")
    print("Note: picasso.t7 and monet.t7 require training your own model.")
    print("See README.md for instructions.")
