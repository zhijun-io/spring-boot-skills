#!/usr/bin/env python3
"""Skill layout gate: every skills/<name>/SKILL.md needs matching frontmatter, a trigger-rich
description, no dead relative links, and a row in the SKILLS.md catalog."""
import glob, os, re, sys

errors = []

for skill_dir in sorted(glob.glob('skills/*/')):
    skill = os.path.basename(skill_dir.rstrip('/'))
    manifest = os.path.join(skill_dir, 'SKILL.md')
    if not os.path.exists(manifest):
        errors.append(f'{skill}: missing SKILL.md')
        continue
    text = open(manifest).read()
    front = re.match(r'\A---\n(.*?)\n---\n', text, re.S)
    if not front:
        errors.append(f'{skill}: SKILL.md has no frontmatter')
        continue
    body = text[front.end():]
    name = re.search(r'^name:[ \t]*(\S.*)$', front.group(1), re.M)
    desc = re.search(r'^description:[ \t]*(.*?)(?=^[a-z_-]+:|\Z)', front.group(1) + '\n', re.M | re.S)
    if not name or name.group(1).strip() != skill:
        errors.append(f"{skill}: frontmatter name is {name.group(1).strip() if name else 'missing'}, must match the directory")
    description = ' '.join((desc.group(1) if desc else '').replace('>-', '').split())
    if not description:
        errors.append(f'{skill}: empty description — the agent never loads this skill')
    elif len(description) > 1024:
        errors.append(f'{skill}: description is {len(description)} chars, keep it under 1024')
    if not body.strip():
        errors.append(f'{skill}: SKILL.md has no body')

documents = sorted(set(glob.glob('SKILL.md') + glob.glob('README.md') + glob.glob('skills/**/*.md', recursive=True)))
for doc in documents:
    base = os.path.dirname(doc)
    for target in re.findall(r'\]\(([^)#h][^)]*)\)', open(doc).read()):
        if not os.path.exists(os.path.normpath(os.path.join(base, target))):
            errors.append(f'{doc}: broken link {target}')

catalog = 'SKILLS.md'
if os.path.exists(catalog):
    listed = set(re.findall(r'`([a-z0-9-]+)`', open(catalog).read()))
    actual = {os.path.basename(d.rstrip('/')) for d in glob.glob('skills/*/')}
    if not actual <= listed:
        errors.append(f'{catalog}: missing skill(s) {sorted(actual - listed)}')

for e in errors:
    print(f'::error::{e}', file=sys.stderr)
print(f'validated {len(glob.glob("skills/*/"))} skill(s), {len(glob.glob("skills/**/*.md", recursive=True))} markdown file(s)')
sys.exit(1 if errors else 0)
