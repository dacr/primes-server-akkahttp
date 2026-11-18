{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";
    utils.url = "github:numtide/flake-utils";
    sbt.url = "github:zaninime/sbt-derivation";
    sbt.inputs.nixpkgs.follows = "nixpkgs";
  };

  outputs = { self, nixpkgs, utils, sbt }:
  utils.lib.eachDefaultSystem (system:
  let
    pkgs = import nixpkgs { inherit system; };
    jdk = pkgs.jdk25;
    jdkHeadless = pkgs.jdk25_headless;
    sbtJdk = pkgs.sbt.override { jre = jdk; };
    sbtJdkHeadless = pkgs.sbt.override { jre = jdkHeadless; };
    metalsJdk = pkgs.metals.override { jre = jdk; };
  in {
    # ---------------------------------------------------------------------------
    # nix develop
    devShells.default = pkgs.mkShell {
      buildInputs = [sbtJdk metalsJdk jdk pkgs.hello];
    };

    # ---------------------------------------------------------------------------
    # nix build
    packages.default = (sbt.mkSbtDerivation.${system}.withOverrides { sbt = sbtJdkHeadless; }) {
      pname = "nix-primes";
      version = builtins.elemAt (builtins.match ''[^"]+"(.*)".*'' (builtins.readFile ./version.sbt)) 0;
      depsSha256 = "sha256-Q4xb9qYpOYk5Hf8sCe8r2jULLEPVX5DDxw/OphCR72I=";

      src = ./.;

      buildInputs = [sbtJdkHeadless jdkHeadless pkgs.makeWrapper];

      buildPhase = "sbt Universal/packageZipTarball";

      installPhase = ''
          mkdir -p $out
          tar xf target/universal/primes-server-akkahttp.tgz --directory $out
          makeWrapper $out/bin/primes-server-akkahttp $out/bin/nix-primes \
            --set JAVA_HOME ${jdkHeadless.home} \
            --set PATH ${pkgs.lib.makeBinPath [
              pkgs.gnused
              pkgs.gawk
              pkgs.coreutils
              pkgs.bash
              jdkHeadless
            ]}
      '';
    };

    # ---------------------------------------------------------------------------
    # simple nixos services integration
    nixosModules.default = { config, pkgs, lib, ... }: {
      options = {
        services.primes = {
          enable = lib.mkEnableOption "primes";
          user = lib.mkOption {
            type = lib.types.str;
            description = "User name that will run the primes service";
          };
          ip = lib.mkOption {
            type = lib.types.str;
            description = "Listening network interface - 0.0.0.0 for all interfaces";
            default = "127.0.0.1";
          };
          port = lib.mkOption {
            type = lib.types.int;
            description = "Service primes listing port";
            default = 8080;
          };
          url = lib.mkOption {
            type = lib.types.str;
            description = "How this service is known/reached from outside";
            default = "http://127.0.0.1:8080";
          };
          prefix = lib.mkOption {
            type = lib.types.str;
            description = "Service primes url prefix";
            default = "";
          };
          datastore = lib.mkOption {
            type = lib.types.str;
            description = "where primes stores its data";
            default = "/tmp/primes-cache-data";
          };
          max-count = lib.mkOption {
            type = lib.types.str;
            description = "How many primes to compute in background (BigInt)";
            default = "1000000000";
          };
          max-limit = lib.mkOption {
            type = lib.types.str;
            description = "Stop primes background compute after this value";
            default = "9223372036854775807";
          };
        };
      };
      config = lib.mkIf config.services.primes.enable {
        systemd.tmpfiles.rules = [
              "d ${config.services.primes.datastore} 0750 ${config.services.primes.user} ${config.services.primes.user} -"
        ];
        systemd.services.primes = {
          description = "Primes service";
          environment = {
            PRIMES_LISTEN_IP   = config.services.primes.ip;
            PRIMES_LISTEN_PORT = (toString config.services.primes.port);
            PRIMES_PREFIX      = config.services.primes.prefix;
            PRIMES_URL         = config.services.primes.url;
            PRIMES_STORE_PATH  = config.services.primes.datastore;
            PRIMES_MAX_COUNT   = config.services.primes.max-count;
            PRIMES_MAX_LIMIT   = config.services.primes.max-limit;
          };
          serviceConfig = {
            ExecStart = "${self.packages.${pkgs.system}.default}/bin/nix-primes";
            User = config.services.primes.user;
            Restart = "on-failure";
          };
          wantedBy = [ "multi-user.target" ];
        };
      };
    };
    # ---------------------------------------------------------------------------

  });
}
