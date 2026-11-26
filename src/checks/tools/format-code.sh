#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../../.."

case "${1:-}" in
    '') java_args=(--replace); kotlin_args=(); python_args=(); groovy_flag=--write ;;
    --check) java_args=(--dry-run --set-exit-if-changed); kotlin_args=(--dry-run --set-exit-if-changed); python_args=(--check); groovy_flag=--check ;;
    *) echo 'Usage: bash src/checks/tools/format-code.sh [--check]' >&2; exit 2 ;;
esac

cache=${FORMATTER_CACHE:-.tools/formatters}
black=${BLACK:-black}
"$black" --required-version 26.5.1 --version >/dev/null
mkdir -p -- "$cache"

fetch() {
    local path=$1 hash=$2 jar="$cache/${1##*/}" tmp
    if ! printf '%s  %s\n' "$hash" "$jar" | sha256sum --check --status; then
        tmp=$(mktemp "$cache/.download.XXXXXX")
        if ! curl --fail --silent --show-error --location --max-time 120 \
            "https://repo.maven.apache.org/maven2/$path" -o "$tmp" \
            || ! printf '%s  %s\n' "$hash" "$tmp" | sha256sum --check --status; then
            rm -f -- "$tmp"
            printf 'Could not fetch verified formatter %s\n' "$path" >&2
            return 1
        fi
        mv -f -- "$tmp" "$jar"
    fi
    printf '%s\n' "$jar"
}

java_formatter=$(fetch com/google/googlejavaformat/google-java-format/1.35.0/google-java-format-1.35.0-all-deps.jar \
    bfb7f9ead6cd328389bc2da53860443bc0e805dfd08cc889bfdf43b26cb2a6e8)
kotlin_formatter=$(fetch com/facebook/ktfmt/0.62/ktfmt-0.62-with-dependencies.jar \
    9c7e2408a03b3582f162449dbab2dfabcc8d09de1609a9831e4c683d1207de01)

find src i18n -type f -name '*.java' -print0 | sort -z | xargs -0 java -jar "$java_formatter" --aosp "${java_args[@]}"
java -jar "$kotlin_formatter" --kotlinlang-style "${kotlin_args[@]}" build.gradle.kts settings.gradle.kts
find src/checks -type f -name '*.py' -print0 | sort -z | xargs -0 "$black" --required-version 26.5.1 "${python_args[@]}"

groovy_modules="$cache/groovy/node_modules"
prettier_version=3.6.2
groovy_version=0.2.1
if ! node -e '
    const [path, prettier, groovy] = process.argv.slice(1);
    const version = name => require(`${path}/${name}/package.json`).version;
    if (version("prettier") !== prettier || version("prettier-plugin-groovy") !== groovy) process.exit(1);
' "$(realpath -m "$groovy_modules")" "$prettier_version" "$groovy_version" 2>/dev/null; then
    npm install --prefix "$cache/groovy" --no-save --package-lock=false \
        --ignore-scripts --no-audit --no-fund "prettier@$prettier_version" "prettier-plugin-groovy@$groovy_version"
fi
node "$groovy_modules/prettier/bin/prettier.cjs" \
    --plugin "$(realpath "$groovy_modules/prettier-plugin-groovy/dist/index.cjs")" \
    --parser groovy "$groovy_flag" 'src/checks/mock/**/*.gradle'
