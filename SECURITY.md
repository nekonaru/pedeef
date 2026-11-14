# Security Policy

## Supported versions

| Version | Supported |
|---------|-----------|
| 1.0.x   | Yes       |

## Reporting a vulnerability

Please report suspected vulnerabilities privately, not in a public issue.

- Use GitHub's private vulnerability reporting on this repository (Security tab, "Report a vulnerability"), or
- open an issue asking for a private channel if you cannot use that.

Please include: what you found, how to reproduce it, and the possible impact. Expect an acknowledgement within 7 days.

## Scope

Pedeef has exactly one network call: an optional "Check for updates" GET request to GitHub, made only when you tap it. It stores your files in the public Download/Pedeef folder (or inside its private storage if you deny access). Findings that matter most:

- Ways to read or modify files outside Pedeef's storage areas.
- Ways to leak a file to another app without consent.
- Crashes or data corruption in the PDF engine triggered by a malicious file.
- Any way the app could send data off the device beyond the update check.

## Out of scope

- Attacks that require a compromised device or a rooted phone.
- Issues in the PDF files you create yourself or receive from others.
