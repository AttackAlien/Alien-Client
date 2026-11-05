using System.Text.Json;
using AlienLauncher.Core;
using Xunit;
namespace AlienLauncher.Tests;

public sealed class NowPlayingTests
{
    [Fact] public void NonPlayingStateClearsListeningMetadata()
    {
        var s = new NowPlayingSnapshot { Status = "idle", Title = "Private title", PositionMs = 123, Artwork = new('a',64) }.Normalize();
        Assert.Empty(s.Title); Assert.Null(s.PositionMs); Assert.Empty(s.Artwork);
    }
    [Fact] public void UnknownTimelineStaysUnknownAndPositionClampsToDuration()
    {
        var s = new NowPlayingSnapshot { Status="playing", PositionMs=30000, DurationMs=12000 }.Normalize();
        Assert.Equal(12000,s.PositionMs); Assert.Null((s with {DurationMs=0}).Normalize().DurationMs);
        Assert.Null(new NowPlayingSnapshot {Status="playing"}.Normalize().PositionMs);
    }
    [Fact] public void InvalidRatesArtworkAndFormattingAreRepaired()
    {
        var s = new NowPlayingSnapshot { Status="paused", Rate=double.NaN, Title="  Can\u00a7ção\n ", Artwork=null! }.Normalize();
        Assert.Equal(1,s.Rate); Assert.Equal("Canção",s.Title); Assert.Empty(s.Artwork);
    }
    [Fact] public void BrowserSourceNeverClaimsToKnowTheWebsite()
    {
        Assert.Equal("Chrome",NowPlayingSnapshot.SourceName("chrome.exe"));
        Assert.Equal("Edge",NowPlayingSnapshot.SourceName("msedge.exe"));
        Assert.True(NowPlayingSnapshot.MatchesSource("chrome.exe","Browser"));
        Assert.False(NowPlayingSnapshot.MatchesSource("chrome.exe","Spotify"));
        Assert.False(NowPlayingSnapshot.MatchesSource("Spotify.exe","Browser"));
        Assert.Equal("Spotify",NowPlayingSnapshot.SourceName("SpotifyAB.SpotifyMusic_xyz!Spotify"));
    }
    [Fact] public void MetadataAndArtworkAreBounded()
    {
        var s = new NowPlayingSnapshot {Status="playing",Title=new('x',900),Artwork=new('A',64)}.Normalize();
        Assert.Equal(512,s.Title.Length); Assert.Equal(new('a',64),s.Artwork);
    }
    [Fact] public async Task AtomicSnapshotIsCamelCaseUtf8AndLeavesNoTemporaryFile()
    {
        string dir=Path.Combine(Path.GetTempPath(),"alien-media-test-"+Guid.NewGuid().ToString("N"));
        try {
            await NowPlayingSnapshot.WriteAsync(dir,new(){Status="playing",Title="Canção 漢字",Source="Chrome"},CancellationToken.None);
            using var json=JsonDocument.Parse(await File.ReadAllTextAsync(Path.Combine(dir,"now-playing.json")));
            Assert.Equal(1,json.RootElement.GetProperty("schema").GetInt32());
            Assert.Equal("Canção 漢字",json.RootElement.GetProperty("title").GetString());
            Assert.Single(Directory.GetFiles(dir));
        } finally { if(Directory.Exists(dir)) Directory.Delete(dir,true); }
    }
}
